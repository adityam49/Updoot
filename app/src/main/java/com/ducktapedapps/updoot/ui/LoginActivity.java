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
import androidx.appcompat.widget.Toolbar;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.api.authAPI;
import com.ducktapedapps.updoot.api.redditAPI;
import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor;
import com.ducktapedapps.updoot.utils.accountManagement.userManager;
import com.ducktapedapps.updoot.utils.constants;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.login_progress)
    ProgressBar progressBar;
    @BindView(R.id.webView)
    WebView webView;

    @Inject
    Lazy<redditAPI> redditAPILazy;
    @Inject
    Lazy<TokenInterceptor> interceptor;
    @Inject
    Lazy<AccountManager> accountManager;
    @Inject
    Lazy<userManager> userManager;
    @Inject
    Lazy<authAPI> authAPI;
    @Inject
    Lazy<SharedPreferences> sharedPreferences;


    private Token mToken;

    private static final String TAG = "LoginActivity";
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        ((UpdootApplication) getApplication()).getUpdootComponent().inject(this);

        setSupportActionBar(toolbar);

        Log.i(TAG, "onCreate: ");

        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        webView.loadUrl(getAuthUrl());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i(TAG, "onPageStarted: login " + url);
                Uri uri = Uri.parse(url);
                if (uri.getHost() != null && uri.getHost().equals(Uri.parse(constants.redirect_uri).getHost())) {
                    if (uri.getQueryParameter("error") == null) {
                        webView.stopLoading();
                        webView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        final String code = uri.getQueryParameter("code");
                        disposable.add(authAPI.get()
                                        .getUserToken(
                                                constants.TOKEN_ACCESS_URL,
                                                Credentials.basic(constants.client_id, ""),
                                                constants.user_grantType,
                                                code,
                                                constants.redirect_uri
                                        )
                                        .doOnSuccess(token -> {
                                            mToken = token;
                                            token.setAbsolute_expiry();
                                            interceptor.get().setSessionToken(token);
                                        })
                                        .doOnError(throwable -> Log.e(TAG, "onPageStarted: ", throwable))
                                        .map(__ -> redditAPILazy.get())
                                        .flatMap(redditAPI::getUserIdentity)
                                        .doOnSuccess(account -> {
                                            sharedPreferences.get().edit().putString(constants.LOGIN_STATE, account.getName()).apply();
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
        Account user_account = new Account(username, constants.ACCOUNT_TYPE);
        Bundle bundle = new Bundle();
        bundle.putString(constants.USER_TOKEN_REFRESH_KEY, token.getRefresh_token());
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
                .appendQueryParameter("client_id", constants.client_id)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("state", state)
                .appendQueryParameter("redirect_uri", constants.redirect_uri)
                .appendQueryParameter("duration", "permanent")
                .appendQueryParameter("scope", constants.scopes)
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
