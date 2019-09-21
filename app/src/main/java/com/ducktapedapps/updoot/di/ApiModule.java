package com.ducktapedapps.updoot.di;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.api.authAPI;
import com.ducktapedapps.updoot.api.redditAPI;
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor;
import com.ducktapedapps.updoot.utils.accountManagement.userManager;
import com.ducktapedapps.updoot.utils.constants;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import io.reactivex.Single;
import okhttp3.Credentials;
import retrofit2.Retrofit;

@Module(includes = {
        ApplicationModule.class, NetworkModule.class}
)
class ApiModule {
    private static final String TAG = "ApiModule";

    @Singleton
    @Provides
    static authAPI provideAuthAPI(final Lazy<Retrofit> retrofit) {
        return retrofit.get().create(authAPI.class);
    }

    //only for internal module use
    @Singleton
    @Provides
    static redditAPI provideRedditAPIService(Retrofit retrofit) {
        return retrofit.create(redditAPI.class);
    }

    @Reusable
    @Provides
    static AccountManager provideAccountManager(Application application, Lazy<userManager> userManager, SharedPreferences sharedPreferences) {
        AccountManager accountManager = AccountManager.get(application);
        accountManager.addOnAccountsUpdatedListener(accounts -> {
            String currentCachedAccount = sharedPreferences.getString(constants.LOGIN_STATE, null);
            boolean currentAccountRemoved = true;
            for (Account account : accounts) {
                Log.i(TAG, "provideAccountManager: account found " + account.name);
                if (account.name.equals(currentCachedAccount)) {
                    currentAccountRemoved = false;
                    break;
                }
            }
            if (currentAccountRemoved) {
                if (currentCachedAccount != null && !currentCachedAccount.equals(constants.ANON_USER)) {
                    Log.i(TAG, "provideAccountManager: setting current account as an        on");
                    userManager.get().setCurrentUser(constants.ANON_USER, null);
                    if (userManager.get().getmListener() != null)
                        userManager.get().getmListener().onCurrentAccountRemoved();
                }
            }

        }, null, true);
        return accountManager;
    }

    @Provides
    static Single<redditAPI> provideRedditAPI(final Lazy<redditAPI> redditAPILazy, final Lazy<authAPI> authAPILazy, TokenInterceptor interceptor, userManager userManager, SharedPreferences sharedPreferences, AccountManager accountManager) {
        Account account = userManager.getCurrentUser();
        if (account == null || ((interceptor.getTokenExpiry() == null || interceptor.getTokenExpiry() < System.currentTimeMillis()) && account.name.equals(constants.ANON_USER))) {
            //first app launch OR fresh app launch with anonymous account logged in or userless token expired
            return authAPILazy.get()
                    .getUserLessToken(
                            constants.TOKEN_ACCESS_URL,
                            Credentials.basic(constants.client_id, ""),
                            constants.userLess_grantType,
                            sharedPreferences.getString(constants.DEVICE_ID_KEY, null))
                    .doOnSuccess(token -> {
                        token.setAbsolute_expiry();
                        userManager.setCurrentUser(constants.ANON_USER, token);
                    })
                    .doOnError(throwable -> Log.e(TAG, "provideRedditAPI: ", throwable))
                    .map(__ -> redditAPILazy.get());
        } else {
            //fresh app start but not the fresh install
            if (interceptor.getTokenExpiry() == null || interceptor.getTokenExpiry() < System.currentTimeMillis()) {
                return authAPILazy.get()
                        .getRefreshedToken(
                                constants.TOKEN_ACCESS_URL,
                                Credentials.basic(constants.client_id, ""),
                                constants.user_refresh_grantType,
                                accountManager.getUserData(account, constants.USER_TOKEN_REFRESH_KEY))
                        .doOnSuccess(token -> {
                            token.setAbsolute_expiry();
                            userManager.updateUserSessionData(token);
                        })
                        .doOnError(throwable -> Log.e(TAG, "provideRedditAPI: ", throwable))
                        .map(__ -> redditAPILazy.get());
            }
        }
        return Single.just(redditAPILazy.get());
    }
}

