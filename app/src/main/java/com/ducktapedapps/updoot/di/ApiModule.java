package com.ducktapedapps.updoot.di;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.api.AuthAPI;
import com.ducktapedapps.updoot.api.RedditAPI;
import com.ducktapedapps.updoot.utils.Constants;
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor;
import com.ducktapedapps.updoot.utils.accountManagement.UserManager;

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
    static AuthAPI provideAuthAPI(final Lazy<Retrofit> retrofit) {
        return retrofit.get().create(AuthAPI.class);
    }

    //only for internal module use
    @Singleton
    @Provides
    static RedditAPI provideRedditAPIService(Retrofit retrofit) {
        return retrofit.create(RedditAPI.class);
    }

    @Reusable
    @Provides
    static AccountManager provideAccountManager(Application application, Lazy<UserManager> userManager, SharedPreferences sharedPreferences) {
        AccountManager accountManager = AccountManager.get(application);
        accountManager.addOnAccountsUpdatedListener(accounts -> {
            String currentCachedAccount = sharedPreferences.getString(Constants.LOGIN_STATE, null);
            boolean currentAccountRemoved = true;
            for (Account account : accounts) {
                Log.i(TAG, "provideAccountManager: Account found " + account.name);
                if (account.name.equals(currentCachedAccount)) {
                    currentAccountRemoved = false;
                    break;
                }
            }
            if (currentAccountRemoved) {
                if (currentCachedAccount != null && !currentCachedAccount.equals(Constants.ANON_USER)) {
                    Log.i(TAG, "provideAccountManager: setting current Account as an        on");
                    userManager.get().setCurrentUser(Constants.ANON_USER, null);
                    if (userManager.get().getmListener() != null)
                        userManager.get().getmListener().onCurrentAccountRemoved();
                }
            }

        }, null, true);
        return accountManager;
    }

    @Provides
    static Single<RedditAPI> provideRedditAPI(final Lazy<RedditAPI> redditAPILazy, final Lazy<AuthAPI> authAPILazy, TokenInterceptor interceptor, UserManager userManager, SharedPreferences sharedPreferences, AccountManager accountManager) {
        Account account = userManager.getCurrentUser();
        if (account == null || ((interceptor.getTokenExpiry() == null || interceptor.getTokenExpiry() < System.currentTimeMillis()) && account.name.equals(Constants.ANON_USER))) {
            //first app launch OR fresh app launch with anonymous Account logged in or userless token expired
            return authAPILazy.get()
                    .getUserLessToken(
                            Constants.TOKEN_ACCESS_URL,
                            Credentials.basic(Constants.client_id, ""),
                            Constants.userLess_grantType,
                            sharedPreferences.getString(Constants.DEVICE_ID_KEY, null))
                    .doOnSuccess(token -> {
                        token.setAbsolute_expiry();
                        userManager.setCurrentUser(Constants.ANON_USER, token);
                    })
                    .doOnError(throwable -> Log.e(TAG, "provideRedditAPI: ", throwable))
                    .map(__ -> redditAPILazy.get());
        } else {
            //fresh app start but not the fresh install
            if (interceptor.getTokenExpiry() == null || interceptor.getTokenExpiry() < System.currentTimeMillis()) {
                return authAPILazy.get()
                        .getRefreshedToken(
                                Constants.TOKEN_ACCESS_URL,
                                Credentials.basic(Constants.client_id, ""),
                                Constants.user_refresh_grantType,
                                accountManager.getUserData(account, Constants.USER_TOKEN_REFRESH_KEY))
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

