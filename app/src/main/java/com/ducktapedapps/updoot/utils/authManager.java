package com.ducktapedapps.updoot.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.ducktapedapps.updoot.api.login;
import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.model.authVerificationData;

import java.util.Random;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.ducktapedapps.updoot.utils.constants.DEVICE_ID_KEY;
import static com.ducktapedapps.updoot.utils.constants.TOKEN_SHARED_PREFS_KEY;
import static com.ducktapedapps.updoot.utils.constants.userLess_grantType;


public class authManager {

    private static final String TAG = "authManager";

    public static Single<Token> authenticate(Application application) {

        Token token;
        if (tokenManager.isUserLoggedIn(application)) {
            token = tokenManager.getUserToken(application);
            assert token != null;
            if (token.getAbsolute_expiry() > System.currentTimeMillis()) {
                Log.i(TAG, "stored user token is not expired");
                return Single.just(token);
            } else {
                Log.i(TAG, "user token expired");
                return authManager.refreshUserToken(application);
            }
        } else {
            token = tokenManager.getUserLessToken(application);
            if (token != null) {
                if (token.getAbsolute_expiry() > System.currentTimeMillis()) {
                    Log.i(TAG, "stored userless token is not expired");
                    return Single.just(token);
                } else {
                    Log.i(TAG, "userless token expired");
                    return authManager.fetchNewUserLessToken(application);
                }
            } else {
                Log.i(TAG, "authenticate: fetching userless token for first time");
                return authManager.fetchNewUserLessToken(application);
            }
        }
    }


    public static Single<Token> fetchNewUserLessToken(Application application) {
        Log.i(TAG, "userLess: fetching new token");

        login login = retrofitClient.createLoginService();

        final SharedPreferences sharedPreferences = application.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        String device_id = sharedPreferences.getString(DEVICE_ID_KEY, null);

        if (device_id == null) {
            device_id = randomString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICE_ID_KEY, device_id);
            editor.apply();
        }

        return login
                .getUserLessToken(userLess_grantType, device_id)
                .doOnSuccess(token -> tokenManager.saveToken(token, application));

    }

    public static Single<Token> user(authVerificationData data) {
        Log.i(TAG, "user: fetching new Token");

        Uri redirectedUrl = Uri.parse(data.getAuthUrl());
        String code = redirectedUrl.getQueryParameter("code");
        String state = redirectedUrl.getQueryParameter("state");

        //reddit api guideline to check state before sending and after receiving are same
        //https://github.com/reddit-archive/reddit/wiki/oauth2#Token%20Retrieval -> see Token Retrieval (code flow)
        if (state != null && state.equals(data.getState())) {
            Log.i(TAG, "user: state checks out");
            login login = retrofitClient.createLoginService();

            return login
                    .getUserToken(constants.user_grantType, code, constants.redirect_uri)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess(token -> Log.i(TAG, "user: " + token));
        }
        return null;

    }

    public static Single<Token> refreshUserToken(Application application) {
        login login = retrofitClient.createLoginService();
        Token token = tokenManager.getUserToken(application);
        Log.i(TAG, "refreshUserToken: refresh token is " + token.getRefresh_token());
        return login
                .getRefreshedToken(constants.user_refresh_grantType, token.getRefresh_token())
                .doOnSuccess(newToken -> tokenManager.saveUserToken(newToken, application));

    }

    public static authVerificationData getAuthUrlWithState() {
        String state = randomString();
        String url = new Uri.Builder()
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
        return new authVerificationData(url, state);
    }

    //25 char long random string
    private static String randomString() {
        String alphabets = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            builder.append(alphabets.charAt(random.nextInt(26)));
        }

        return builder.toString();
    }
}
