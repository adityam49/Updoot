package com.ducktapedapps.updoot.ui;

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
import com.ducktapedapps.updoot.model.authVerificationData;
import com.ducktapedapps.updoot.utils.authManager;
import com.ducktapedapps.updoot.utils.constants;
import com.ducktapedapps.updoot.utils.tokenManager;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "LoginActivity";
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_actvity);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.i(TAG, "onCreate: ");
        authVerificationData data = authManager.getAuthUrlWithState();

        WebView webView = findViewById(R.id.webView);

        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();


        webView.loadUrl(data.getAuthUrl());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i(TAG, "onPageStarted: login " + url);
                Uri uri = Uri.parse(url);
                if (uri.getHost().equals(Uri.parse(constants.redirect_uri).getHost())) {
                    webView.stopLoading();
                    data.setAuthUrl(url);

                    disposable.add(
                            authManager
                                    .user(data)
                                    .subscribeOn(Schedulers.io())
                                    .doOnSuccess(token -> {
                                        tokenManager.saveUserToken(token, LoginActivity.this);
                                        setResult(RESULT_OK);
                                        finish();
                                    })
                                    .doOnError(error -> Log.e(TAG, "onPageStarted: ", error.getCause()))
                                    .subscribe()
                    );
                }
            }
        });
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
