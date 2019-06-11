package com.ducktapedapps.updoot.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.model.Token;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.ducktapedapps.updoot.utils.constants.TOKEN_SHARED_PREFS_KEY;
import static com.ducktapedapps.updoot.utils.constants.USERLESS_TOKEN_EXPIRY_KEY;
import static com.ducktapedapps.updoot.utils.constants.USERLESS_TOKEN_KEY;

public class tokenManager {

    private static final String TAG = "tokenManager";

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

    public static Token getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(USERLESS_TOKEN_KEY, null);
        long absExpiry = sharedPreferences.getLong(USERLESS_TOKEN_EXPIRY_KEY, 0);

        if (accessToken != null || absExpiry != 0) {
            return new Token(accessToken, absExpiry);
        } else {
            return null;
        }
    }

    public static boolean checkTokenValidity(Context context) {
        Token token = getToken(context);
        if (token != null) {
            return System.currentTimeMillis() < token.getAbsolute_expiry();
        } else {
            return false;
        }
    }
}
