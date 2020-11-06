package com.ducktapedapps.updoot.di

import android.accounts.AccountManager
import android.content.Context
import com.ducktapedapps.updoot.data.remote.AuthAPI
import com.ducktapedapps.updoot.data.remote.RedditAPI
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class ApiModule {
    @Provides
    fun provideAuthAPI(retrofit: Retrofit): AuthAPI = retrofit.create(AuthAPI::class.java)

    @Singleton
    @Provides
    fun provideRedditAPIService(retrofit: Retrofit): RedditAPI = retrofit.create(RedditAPI::class.java)

    @Provides
    fun provideAccountManager(context: Context): AccountManager = AccountManager.get(context)
}