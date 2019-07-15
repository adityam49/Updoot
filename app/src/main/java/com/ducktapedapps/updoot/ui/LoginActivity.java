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
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.utils.constants;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;

public class LoginActivity extends AppCompatActivity {

    @Inject
    authAPI authAPI;
    @Inject
    SharedPreferences sharedPreferences;

    private String state;
    private Token mToken;

    private static final String TAG = "LoginActivity";
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_actvity);

        UpdootComponent component = ((UpdootApplication) getApplication()).getUpdootComponent();
        component.inject(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.i(TAG, "onCreate: ");


        ProgressBar progressBar = findViewById(R.id.login_progress);
        WebView webView = findViewById(R.id.webView);

        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        String url = getAuthUrl();

        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i(TAG, "onPageStarted: login " + url);
                Uri uri = Uri.parse(url);
                if (uri.getHost() != null && uri.getHost().equals(Uri.parse(constants.redirect_uri).getHost())) {
                    webView.stopLoading();
                    webView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    final String code = uri.getQueryParameter("code");
                    disposable.add(authAPI
                            .getUserToken(constants.TOKEN_ACCESS_URL, Credentials.basic(constants.client_id, ""), constants.user_grantType, code, constants.redirect_uri)
                            .doOnSuccess(token -> {
                                mToken = token;
                                token.setAbsolute_expiry();
                                component.getTokenInterceptor().setSessionToken(token, "temp");
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(constants.LOGIN_STATE, "temp");
                                editor.apply();
                            })
                            .doOnError(throwable -> Log.e(TAG, "onPageStarted: ", throwable))
                            .flatMap(__ -> component.getRedditAPI())
                            .flatMap(redditAPI::getUserIdentity)
                            .doOnSuccess(account -> {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(constants.LOGIN_STATE, account.getName());
                                editor.apply();
                                component.getTokenInterceptor().setSessionToken(mToken, account.getName());
                                createAccount(account.getName(), mToken);
                                setResult(RESULT_OK);
                                finish();
                            })
                            .doOnError(throwable -> Log.e(TAG, "onPageStarted: ", throwable))
                            .subscribeOn(Schedulers.io())
                            .subscribe()
                    );
                }
            }
        });
    }


    public void createAccount(String username, Token token) {
        Account user_account = new Account(username, constants.ACCOUNT_TYPE);
        Bundle bundle = new Bundle();
        bundle.putString(constants.USER_TOKEN_REFRESH_KEY, token.getRefresh_token());
        AccountManager am = AccountManager.get(this);
        am.addAccountExplicitly(user_account, null, bundle);
        am.setAuthToken(user_account, "full_access", token.getAccess_token());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    // for not reloading on config change
    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public String getAuthUrl() {
        state = UUID.randomUUID().toString();
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

}
