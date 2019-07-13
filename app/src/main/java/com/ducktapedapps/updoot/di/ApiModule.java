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

    //only for internal module use
    @Singleton
    @Provides
    static redditAPI provideRedditAPIService(Retrofit retrofit) {
        return retrofit.create(redditAPI.class);
    }

    //basically token refresh check for every api call
    @Provides
    static Single<redditAPI> providesRedditAPI(SharedPreferences sharedPreferences, TokenInterceptor interceptor, Retrofit retrofit) {
        String access_token;
        long token_expiry;
        String state = sharedPreferences.getString(constants.LOGIN_STATE, null);
        assert state != null;
        if (state.equals(constants.LOGGED_OUT_STATE)) {
            access_token = sharedPreferences.getString(constants.USERLESS_TOKEN_KEY, null);
            token_expiry = sharedPreferences.getLong(constants.USERLESS_TOKEN_EXPIRY_KEY, 0);

            if (access_token != null && token_expiry > System.currentTimeMillis() / 1000) {
//            using cached token
                interceptor.setSessionToken(access_token);
                return Single.just(provideRedditAPIService(retrofit));
            } else {
                //fetching new token and saving to shared prefs
                return provideAuthAPI(retrofit)
                        .getUserLessToken(
                                constants.TOKEN_ACCESS_URL,
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
                        .map(__ -> provideRedditAPIService(retrofit));
            }
        } else {
            access_token = sharedPreferences.getString(constants.USER_TOKEN_KEY, null);
            token_expiry = sharedPreferences.getLong(constants.USER_TOKEN_EXPIRY_KEY, 0);
            String refresh_token = sharedPreferences.getString(constants.USER_TOKEN_REFRESH_KEY, null);

            if (token_expiry > System.currentTimeMillis() / 1000) {
                //using cached token
                interceptor.setSessionToken(access_token);
                return Single.just(provideRedditAPIService(retrofit));
            } else {
                return provideAuthAPI(retrofit)
                        .getRefreshedToken(constants.TOKEN_ACCESS_URL, Credentials.basic(constants.client_id, ""), constants.user_refresh_grantType, refresh_token)
                        .doOnSuccess(token -> {
                            interceptor.setSessionToken(token.getAccess_token());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(constants.USER_TOKEN_KEY, token.getAccess_token());
                            editor.putLong(constants.USER_TOKEN_EXPIRY_KEY, token.getAbsolute_expiry());
                            editor.apply();
                        })
                        .doOnError(throwable -> Log.e(TAG, "providesAPI: ", throwable))
                        .map(__ -> provideRedditAPIService(retrofit));
            }
        }
        // returned redditAPI object is the same in both cases but is wrapped around in new Single observable for every call
        // only difference is the token manipulation done in token interceptor object which is a singleton
    }

}
