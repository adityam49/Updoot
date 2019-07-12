package com.ducktapedapps.updoot.di;

import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.api.authAPI;
import com.ducktapedapps.updoot.api.redditAPI;
import com.ducktapedapps.updoot.utils.TokenInterceptor;
import com.ducktapedapps.updoot.utils.constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Single;
import okhttp3.Credentials;
import retrofit2.Retrofit;

@Module(includes = {ApplicationModule.class, NetworkModule.class})
public class ApiModule {
    private static final String TAG = "ApiModule";

    @Singleton
    @Provides
    static authAPI provideAuthAPI(Retrofit retrofit) {
        return retrofit.create(authAPI.class);
    }

    @Singleton
    @Provides
    static redditAPI provideRedditAPI(Retrofit retrofit) {
        return retrofit.create(redditAPI.class);
    }

    @Provides
    static Single<redditAPI> provideRedditApi(SharedPreferences sharedPreferences, TokenInterceptor interceptor, Retrofit retrofit) {
        String access_token = sharedPreferences.getString(constants.USERLESS_TOKEN_KEY, null);
        long token_expiry = sharedPreferences.getLong(constants.USERLESS_TOKEN_EXPIRY_KEY, 0);
        if (access_token != null && token_expiry > System.currentTimeMillis() / 1000) {
//            using cached token
            interceptor.setSessionToken(access_token);
            return Single.just(provideRedditAPI(retrofit));
        } else {
            //fetching new token and saving to shared prefs
            return provideAuthAPI(retrofit)
                    .getUserLessToken(
                            constants.token_access_base_url,
                            Credentials.basic(constants.client_id, ""),
                            constants.userLess_grantType,
                            sharedPreferences.getString(constants.DEVICE_ID_KEY, null)
                    ).doOnSuccess(token -> {
                        interceptor.setSessionToken(token.getAccess_token());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong(constants.USERLESS_TOKEN_EXPIRY_KEY, token.getAbsolute_expiry());
                        editor.putString(constants.USERLESS_TOKEN_KEY, token.getAccess_token());
                        editor.apply();
                    })
                    .doOnError(throwable -> Log.e(TAG, "getUserLessToken: ", throwable))
                    .map(__ -> provideRedditAPI(retrofit));
        }
        // returned redditAPI object is the same in both cases but is wrapped around in new Single observable for every call
        // only difference is the token manipulation done in token interceptor object which is a singleton
    }
}
