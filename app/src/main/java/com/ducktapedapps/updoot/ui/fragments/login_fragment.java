package com.ducktapedapps.updoot.ui.fragments;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.model.authVerificationData;
import com.ducktapedapps.updoot.utils.authManager;
import com.ducktapedapps.updoot.utils.constants;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class login_fragment extends DialogFragment {
    private static final String TAG = "login_fragment";

    public static login_fragment newInstance() {

        Bundle args = new Bundle();

        login_fragment fragment = new login_fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        authVerificationData data = authManager.getAuthUrlWithState();

        WebView webView = view.findViewById(R.id.webView);

        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();


        webView.loadUrl(data.getAuthUrl());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Uri uri = Uri.parse(url);

                if (uri.getHost().equals(Uri.parse(constants.redirect_uri).getHost())) {
                    data.setAuthUrl(url);
                    webView.stopLoading();
                    authManager
                            .user(data)
                            .subscribe(new SingleObserver<Token>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Token token) {
                                    if (token == null) {
                                        Log.i(TAG, "onSuccess: null token");
                                    } else {
                                        Log.i(TAG, "onSuccess: " + token);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "onError: ", e);
                                }
                            });
                    dismiss();
                }
                super.onPageStarted(view, url, favicon);
            }
        });

        return view;
    }


}
