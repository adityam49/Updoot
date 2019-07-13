package com.ducktapedapps.updoot.ui;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.api.authAPI;
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.utils.constants;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;

public class LoginActivity extends AppCompatActivity {

    @Inject
    authAPI authAPI;
    @Inject
    SharedPreferences sharedPreferences;

    private String state;

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
                    final String code = uri.getQueryParameter("code");
                    disposable.add(authAPI
                            .getUserToken(constants.TOKEN_ACCESS_URL, Credentials.basic(constants.client_id, ""), constants.user_grantType, code, constants.redirect_uri)
                            .doOnSuccess(token -> {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(constants.LOGIN_STATE, constants.LOGGED_IN_STATE);
                                editor.putString(constants.USER_TOKEN_KEY, token.getAccess_token());
                                editor.putString(constants.USER_TOKEN_REFRESH_KEY, token.getRefresh_token());
                                editor.putLong(constants.USER_TOKEN_EXPIRY_KEY, token.getAbsolute_expiry());
                                editor.apply();
                                component.getTokenInterceptor().setSessionToken(token.getAccess_token());
                                setResult(RESULT_OK);
                                finish();
                            })
                            .doOnError(throwable -> Log.e(TAG, "onPageStarted: ", throwable))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe()
                    );
                }
            }
        });
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
}
