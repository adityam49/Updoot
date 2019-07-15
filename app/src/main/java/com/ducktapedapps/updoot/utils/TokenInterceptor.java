package com.ducktapedapps.updoot.utils;

import android.util.Log;

import com.ducktapedapps.updoot.model.Token;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//Adds token to endpoint api caches current user session info
@Singleton
public class TokenInterceptor implements Interceptor {
    private static final String TAG = "TokenInterceptor";
    private String sessionToken;
    private Long tokenExpiry;
    private String user;
    private boolean accountChanged;

    @Inject
    public TokenInterceptor() {
        this.accountChanged = false;
    }

    public boolean isAccountChanged() {
        return accountChanged;
    }

    public Long getTokenExpiry() {
        return tokenExpiry;
    }

    public void setAccountChanged() {
        this.accountChanged = true;
    }

    public String getUser() {
        return user;
    }

    public void setSessionToken(Token token, String user) {
        this.accountChanged = false;
        this.user = user;
        this.sessionToken = token.getAccess_token();
        this.tokenExpiry = token.getAbsolute_expiry();
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
                Log.i(TAG, "intercept: adding user " + getUser() + " token " + sessionToken);
                requestBuilder.addHeader("Authorization", "bearer " + sessionToken);
            } else {
                Log.e(TAG, " intercept: session token is null ");
            }
        }
        return chain.proceed(requestBuilder.build());
    }
}

