package com.ducktapedapps.updoot.di

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.ducktapedapps.updoot.api.AuthAPI
import com.ducktapedapps.updoot.api.RedditAPI
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import com.ducktapedapps.updoot.utils.accountManagement.UserManager
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.Single
import okhttp3.Credentials
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class ApiModule {
    @Singleton
    @Provides
    fun provideAuthAPI(retrofit: Lazy<Retrofit>): AuthAPI {
        return retrofit.get().create(AuthAPI::class.java)
    }

    //only for internal module use

    @Singleton
    @Provides
    fun provideRedditAPIService(retrofit: Retrofit): RedditAPI {
        return retrofit.create(RedditAPI::class.java)
    }


    @Reusable
    @Provides
    fun provideAccountManager(application: Application?, userManager: Lazy<UserManager>, sharedPreferences: SharedPreferences): AccountManager {
        val accountManager = AccountManager.get(application)
        accountManager.addOnAccountsUpdatedListener({ accounts: Array<Account> ->
            val currentCachedAccount = sharedPreferences.getString(Constants.LOGIN_STATE, null)
            var currentAccountRemoved = true
            for (account in accounts) {
                if (account.name == currentCachedAccount) {
                    currentAccountRemoved = false
                    break
                }
            }
            if (currentAccountRemoved) {
                if (currentCachedAccount != null && currentCachedAccount != Constants.ANON_USER) {
                    userManager.get().setCurrentUser(Constants.ANON_USER, null)
                    if (userManager.get().getmListener() != null) userManager.get().getmListener().onCurrentAccountRemoved()
                }
            }
        }, null, true)
        return accountManager
    }


    @Provides
    fun provideRedditAPI(redditAPILazy: Lazy<RedditAPI?>, authAPILazy: Lazy<AuthAPI>, interceptor: TokenInterceptor, userManager: UserManager, sharedPreferences: SharedPreferences, accountManager: AccountManager): Single<RedditAPI?> {
        val account = userManager.currentUser
        if (account == null || (interceptor.tokenExpiry == null || interceptor.tokenExpiry < System.currentTimeMillis()) && account.name == Constants.ANON_USER) { //first app launch OR fresh app launch with anonymous Account logged in or userless token expired
            return authAPILazy.get()
                    .getUserLessToken(
                            Constants.TOKEN_ACCESS_URL,
                            Credentials.basic(Constants.client_id, ""),
                            Constants.userLess_grantType,
                            sharedPreferences.getString(Constants.DEVICE_ID_KEY, null) ?: ""
                    )
                    .doOnSuccess { token: Token ->
                        token.setAbsoluteExpiry()
                        userManager.setCurrentUser(Constants.ANON_USER, token)
                    }
                    .doOnError { throwable: Throwable? -> Log.e("provideRedditAPI", "provideRedditAPI: ", throwable) }
                    .map { redditAPILazy.get() }
        } else { //fresh app start but not the fresh install
            if (interceptor.tokenExpiry == null || interceptor.tokenExpiry < System.currentTimeMillis()) {
                return authAPILazy.get()
                        .getRefreshedToken(
                                Constants.TOKEN_ACCESS_URL,
                                Credentials.basic(Constants.client_id, ""),
                                Constants.user_refresh_grantType,
                                accountManager.getUserData(account, Constants.USER_TOKEN_REFRESH_KEY))
                        .doOnSuccess { token: Token ->
                            token.setAbsoluteExpiry()
                            userManager.updateUserSessionData(token)
                        }
                        .doOnError { throwable: Throwable? -> Log.e("provideRedditAPI", "provideRedditAPI: ", throwable) }
                        .map { redditAPILazy.get() }
            }
        }
        return Single.just(redditAPILazy.get())
    }
}