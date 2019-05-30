package com.ducktapedapps.updoot.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.api.login;
import com.ducktapedapps.updoot.model.Token;

import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static com.ducktapedapps.updoot.utils.constants.DEVICE_ID_KEY;
import static com.ducktapedapps.updoot.utils.constants.TOKEN_SHARED_PREFS_KEY;
import static com.ducktapedapps.updoot.utils.constants.userLess_grantType;


public class auth {

    private static final String TAG = "authClass";


    public static Single<Token> userLess(Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String token = tokenManager.getToken(context);
        if (token != null) {
            Log.i(TAG, "userLess: token stored was " + token);
            long token_expiry = tokenManager.getExpiry(context);
            if (token_expiry - 300 > System.currentTimeMillis() / 1000) {
                Log.i(TAG, "userLess: stored token is not expired");
                return Single.just(new Token(token, token_expiry));
            } else {
                Log.i(TAG, "userLess: token expired");
            }
        }

        Retrofit retrofit = retrofitClientGenerator.create();
        login login = retrofit.create(login.class);

        String device_id = sharedPreferences.getString(DEVICE_ID_KEY, null);

        if (device_id == null) {
            device_id = randomString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DEVICE_ID_KEY, device_id);
            editor.apply();
        }

        return login
                .getUserLessToken(userLess_grantType, device_id)
                .subscribeOn(Schedulers.io());

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
