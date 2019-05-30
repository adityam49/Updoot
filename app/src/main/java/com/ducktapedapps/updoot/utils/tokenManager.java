package com.ducktapedapps.updoot.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ducktapedapps.updoot.model.Token;

import static com.ducktapedapps.updoot.utils.constants.TOKEN_SHARED_PREFS_KEY;
import static com.ducktapedapps.updoot.utils.constants.USERLESS_TOKEN_EXPIRY_KEY;
import static com.ducktapedapps.updoot.utils.constants.USERLESS_TOKEN_KEY;

public class tokenManager {

    public static void saveToken(Token token, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(USERLESS_TOKEN_KEY, "").equals(token.getAccess_token())) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(constants.USERLESS_TOKEN_KEY, token.getAccess_token());
        editor.putLong(constants.USERLESS_TOKEN_EXPIRY_KEY, System.currentTimeMillis() / 1000 + token.getExpires_in());
        editor.apply();
    }

    public static Token getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(USERLESS_TOKEN_KEY, null);
        long absExpiry = sharedPreferences.getLong(USERLESS_TOKEN_EXPIRY_KEY, 0);

        if (accessToken == null || absExpiry == 0)
            return null;
        else
            return new Token(accessToken, absExpiry);
    }

    public static boolean checkTokenValidity(Context context) {
        Token token = getToken(context);
        if (token != null) {
            return token.getAbsolute_expiry() - 300 < System.currentTimeMillis() / 1000;
        } else {
            return false;
        }
    }
}
