package com.ducktapedapps.updoot.utils;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//Adds token to endpoint api requests
@Singleton
public class TokenInterceptor implements Interceptor {
    private static final String TAG = "TokenInterceptor";
    private String sessionToken;

    @Inject
    public TokenInterceptor() {
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder();

        if (request.header("Authorization") == null) {
            //Providing credentials
            Log.i(TAG, " intercept: no auth header present thus adding new ones");
            if (sessionToken != null) {
                requestBuilder.addHeader("Authorization", "bearer " + sessionToken);
            } else {
                Log.e(TAG, " intercept: session token is null ");
            }
        }
        return chain.proceed(requestBuilder.build());
    }
}

