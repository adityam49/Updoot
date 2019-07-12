package com.ducktapedapps.updoot.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.model.Token;

import static com.ducktapedapps.updoot.utils.constants.TOKEN_SHARED_PREFS_KEY;
import static com.ducktapedapps.updoot.utils.constants.USERLESS_TOKEN_EXPIRY_KEY;
import static com.ducktapedapps.updoot.utils.constants.USERLESS_TOKEN_KEY;
import static com.ducktapedapps.updoot.utils.constants.USER_TOKEN_EXPIRY_KEY;
import static com.ducktapedapps.updoot.utils.constants.USER_TOKEN_KEY;
import static com.ducktapedapps.updoot.utils.constants.USER_TOKEN_REFRESH_KEY;

public class tokenManager {

    private static final String TAG = "tokenManager";

    public static void saveUserToken(Token token, Context context) {
        Log.i(TAG, "saveUserToken: saving user token " + token);
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(USER_TOKEN_KEY, "").equals(token.getAccess_token())) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(constants.USER_TOKEN_KEY, token.getAccess_token());
        if (token.getRefresh_token() != null) {
            editor.putString(constants.USER_TOKEN_REFRESH_KEY, token.getRefresh_token());
        }
        editor.putLong(constants.USER_TOKEN_EXPIRY_KEY, System.currentTimeMillis() + 3600000);
        editor.apply();
    }

    public static void saveToken(Token token, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(USERLESS_TOKEN_KEY, "").equals(token.getAccess_token())) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(constants.USERLESS_TOKEN_KEY, token.getAccess_token());
        editor.putLong(constants.USERLESS_TOKEN_EXPIRY_KEY, System.currentTimeMillis() + 3600000);
        editor.apply();
    }

    public static Token getUserLessToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(USERLESS_TOKEN_KEY, null);
        long absExpiry = sharedPreferences.getLong(USERLESS_TOKEN_EXPIRY_KEY, 0);

        if (accessToken != null || absExpiry != 0) {
            return new Token(accessToken, absExpiry);
        } else {
            return null;
        }
    }

    public static Token getUserToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(USER_TOKEN_KEY, null);
        long absExpiry = sharedPreferences.getLong(USER_TOKEN_EXPIRY_KEY, 0);
        String refreshToken = sharedPreferences.getString(USER_TOKEN_REFRESH_KEY, null);

        if (accessToken != null || absExpiry != 0) {
            return new Token(accessToken, refreshToken, absExpiry);
        } else {
            return null;
        }
    }

    public static boolean checkUserLessTokenValidity(Context context) {
        Token token = getUserLessToken(context);
        if (token != null) {
            return System.currentTimeMillis() < token.getAbsolute_expiry();
        } else {
            return false;
        }
    }

    public static boolean checkUserTokenValidity(Context context) {
        Token token = getUserToken(context);
        if (token != null) {
            return System.currentTimeMillis() < token.getAbsolute_expiry();
        } else {
            return false;
        }
    }

    public static boolean isUserLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_TOKEN_REFRESH_KEY, null) != null;
    }
}
