package com.ducktapedapps.updoot.di

import android.accounts.AccountManager
import android.content.Context
import com.ducktapedapps.updoot.data.remote.AuthAPI
import com.ducktapedapps.updoot.data.remote.RedditAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class ApiModule {
    @Provides
    fun provideAuthAPI(retrofit: Retrofit): AuthAPI = retrofit.create(AuthAPI::class.java)

    @Singleton
    @Provides
    fun provideRedditAPIService(retrofit: Retrofit): RedditAPI = retrofit.create(RedditAPI::class.java)

    @Provides
    fun provideAccountManager(@ApplicationContext context: Context): AccountManager = AccountManager.get(context)
}