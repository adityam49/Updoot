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

    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USERLESS_TOKEN_KEY, null);
    }

    public static long getExpiry(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(USERLESS_TOKEN_EXPIRY_KEY, 0);
    }
}
