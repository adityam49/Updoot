package com.ducktapedapps.updoot.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.api.AuthAPI;
import com.ducktapedapps.updoot.api.RedditAPI;
import com.ducktapedapps.updoot.databinding.ActivityLoginBinding;
import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.utils.Constants;
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.inject.Inject;

import dagger.Lazy;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;

public class LoginActivity extends AppCompatActivity {

    @Inject
    Lazy<RedditAPI> redditAPILazy;
    @Inject
    Lazy<AuthAPI> authAPI;
    @Inject
    Lazy<TokenInterceptor> interceptor;
    @Inject
    Lazy<AccountManager> accountManager;
    @Inject
    Lazy<SharedPreferences> sharedPreferences;

    private Token mToken;

    private static final String TAG = "LoginActivity";
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        ((UpdootApplication) getApplication()).getUpdootComponent().inject(this);

        setSupportActionBar(binding.toolbar);

        Log.i(TAG, "onCreate: ");

        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        WebView webView = binding.webView;
        final ProgressBar progressBar = binding.loginProgress;
        webView.loadUrl(getAuthUrl());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i(TAG, "onPageStarted: login " + url);
                Uri uri = Uri.parse(url);
                if (uri.getHost() != null && uri.getHost().equals(Uri.parse(Constants.redirect_uri).getHost())) {
                    if (uri.getQueryParameter("error") == null) {
                        webView.stopLoading();
                        webView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        final String code = uri.getQueryParameter("code");
                        disposable.add(authAPI.get()
                                        .getUserToken(
                                                Constants.TOKEN_ACCESS_URL,
                                                Credentials.basic(Constants.client_id, ""),
                                                Constants.user_grantType,
                                                code,
                                                Constants.redirect_uri
                                        )
                                        .doOnSuccess(token -> {
                                            mToken = token;
                                            token.setAbsolute_expiry();
                                            interceptor.get().setSessionToken(token);
                                        })
                                        .doOnError(throwable -> Log.e(TAG, "onPageStarted: ", throwable))
                                        .map(__ -> redditAPILazy.get())
                                        .flatMap(RedditAPI::getUserIdentity)
                                        .doOnSuccess(account -> {
                                            sharedPreferences.get().edit().putString(Constants.LOGIN_STATE, account.getName()).apply();
//                                    component.getTokenInterceptor().setSessionToken(mToken);
                                            createAccount(account.getName(), mToken);
                                            setResult(RESULT_OK);
                                            finish();
                                        })
                                        .doOnError(throwable -> Log.e(TAG, "onPageStarted: ", throwable))
                                        .subscribeOn(Schedulers.io())
                                        .subscribe()
                        );
                    } else {
                        Log.i(TAG, "onPageStarted: ");
                        finish();
                    }
                }
            }
        });
    }


    public void createAccount(String username, Token token) {
        Account user_account = new Account(username, Constants.ACCOUNT_TYPE);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.USER_TOKEN_REFRESH_KEY, token.getRefresh_token());
        accountManager.get().addAccountExplicitly(user_account, null, bundle);
        accountManager.get().setAuthToken(user_account, "full_access", token.getAccess_token());
    }

    // for not reloading on config change
    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public String getAuthUrl() {
        String state = UUID.randomUUID().toString();
        return new Uri.Builder()
                .scheme("https")
                .authority("www.reddit.com")
                .appendPath("api")
                .appendPath("v1")
                .appendPath("authorize.compact")
                .appendQueryParameter("client_id", Constants.client_id)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("state", state)
                .appendQueryParameter("redirect_uri", Constants.redirect_uri)
                .appendQueryParameter("duration", "permanent")
                .appendQueryParameter("scope", Constants.scopes)
                .build()
                .toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }


}
