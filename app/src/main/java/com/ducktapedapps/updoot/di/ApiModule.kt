package com.ducktapedapps.updoot.di

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.content.SharedPreferences
import com.ducktapedapps.updoot.api.AuthAPI
import com.ducktapedapps.updoot.api.RedditAPI
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.UserManager
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class ApiModule {
    @Singleton
    @Provides
    fun provideAuthAPI(retrofit: Retrofit): AuthAPI {
        return retrofit.create(AuthAPI::class.java)
    }

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
                    userManager.get().listener?.onCurrentAccountRemoved()
                }
            }
        }, null, true)
        return accountManager
    }
}