package com.ducktapedapps.updoot.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.api.login;
import com.ducktapedapps.updoot.model.Token;

import java.util.Random;

import io.reactivex.Single;

import static com.ducktapedapps.updoot.utils.constants.DEVICE_ID_KEY;
import static com.ducktapedapps.updoot.utils.constants.TOKEN_SHARED_PREFS_KEY;
import static com.ducktapedapps.updoot.utils.constants.userLess_grantType;


public class authManager {

    private static final String TAG = "authManager";

    public static Single<Token> authenticate(Application application) {
        Token token = tokenManager.getToken(application);
        if (token != null) {
            if (token.getAbsolute_expiry() > System.currentTimeMillis()) {
                Log.i(TAG, "userLess: stored token is not expired");
                return Single.just(token);
            } else {
                Log.i(TAG, "userLess: token expired");
            }
        }
        return authManager.userLess(application);
    }


    private static Single<Token> userLess(Context context) {
        Log.i(TAG, "userLess: fetching new token");
        final SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        login login = retrofitClient.createLoginService();

        String device_id = sharedPreferences.getString(DEVICE_ID_KEY, null);

        if (device_id == null) {
            device_id = randomString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICE_ID_KEY, device_id);
            editor.apply();
        }

        return login
                .getUserLessToken(userLess_grantType, device_id)
                .doOnSuccess(token -> tokenManager.saveToken(token, context));

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
