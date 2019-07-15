package com.ducktapedapps.updoot.di;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
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

@Module(includes = {
        ApplicationModule.class, NetworkModule.class}
)
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

    @Provides
    static AccountManager provideAccountManager(Application application) {
        return AccountManager.get(application);
    }

    //basically token refresh check for every api call
    @Provides
    static Single<redditAPI> provideRedditAPI(AccountManager accountManager, SharedPreferences sharedPreferences, authAPI authAPI, redditAPI redditAPI, TokenInterceptor interceptor) {
        String state = sharedPreferences.getString(constants.LOGIN_STATE, null);
        assert state != null;
        Account account;
        if (state.equals(constants.ANON_USER)) {
            account = new Account(constants.ANON_USER, constants.ACCOUNT_TYPE);
            if (accountManager.getAccounts().length == 0) {
                //for first time app launch
                Log.i(TAG, "providesRedditAPI: for first time app launch");
                return authAPI
                        .getUserLessToken(constants.TOKEN_ACCESS_URL, Credentials.basic(constants.client_id, ""), constants.userLess_grantType, sharedPreferences.getString(constants.DEVICE_ID_KEY, null))
                        .doOnSuccess(token -> {
                            token.setAbsolute_expiry();
                            accountManager.addAccountExplicitly(account, "", null);
                            accountManager.setAuthToken(account, "limited", token.getAccess_token());
                            interceptor.setSessionToken(token, constants.ANON_USER);
                        })
                        .doOnError(throwable -> Log.e(TAG, "newRedditApi: ", throwable))
                        .map(token -> redditAPI);
            } else if (interceptor.isAccountChanged() || interceptor.getTokenExpiry() == null || (interceptor.getTokenExpiry() != null && interceptor.getTokenExpiry() < System.currentTimeMillis())) {
                // userless token refreshing or fresh app launch
                Log.i(TAG, "providesRedditAPI: userless token refreshing or fresh app launch");
                return authAPI
                        .getUserLessToken(constants.TOKEN_ACCESS_URL, Credentials.basic(constants.client_id, ""), constants.userLess_grantType, sharedPreferences.getString(constants.DEVICE_ID_KEY, null))
                        .doOnSuccess(token -> {
                            token.setAbsolute_expiry();
                            accountManager.setAuthToken(account, "limited", token.getAccess_token());
                            interceptor.setSessionToken(token, constants.ANON_USER);
                        }).doOnError(throwable -> Log.e(TAG, "providesRedditAPI: ", throwable))
                        .map(__ -> redditAPI);
            } else {
                //valid userless token
                Log.i(TAG, "providesRedditAPI: valid userless token");
                return Single.just(redditAPI);
            }
        } else {
            //user token
            if (interceptor.isAccountChanged() || interceptor.getTokenExpiry() == null || (interceptor.getTokenExpiry() != null && interceptor.getTokenExpiry() < System.currentTimeMillis())) {
                // user token refreshing or fresh app launch
                Log.i(TAG, "providesRedditAPI: user token refreshing or fresh app launch");
                account = new Account(state, constants.ACCOUNT_TYPE);
                return authAPI
                        .getRefreshedToken(constants.TOKEN_ACCESS_URL,
                                Credentials.basic(constants.client_id, ""),
                                constants.user_refresh_grantType,
                                accountManager.getUserData(account, constants.USER_TOKEN_REFRESH_KEY))
                        .doOnSuccess(token -> {
                            Log.i(TAG, "providesRedditAPI: " + token);
                            token.setAbsolute_expiry();
                            accountManager.setAuthToken(account, "full_access", token.getAccess_token());
                            interceptor.setSessionToken(token, state);
                        })
                        .doOnError(throwable -> Log.e(TAG, "providesRedditAPI: ", throwable))
                        .map(__ -> redditAPI);
            } else {
                Log.i(TAG, "providesRedditAPI: user token valid");
                //valid user token
                return Single.just(redditAPI);
            }
        }
        // returned redditAPI object is the same in both cases but is wrapped around in new Single observable for every call
        // only difference is the token manipulation done in token interceptor object which is a singleton
    }
}
