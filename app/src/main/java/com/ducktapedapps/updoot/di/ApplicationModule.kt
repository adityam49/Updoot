package com.ducktapedapps.updoot.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.accountManagement.RedditClientImpl
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountManager
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ApplicationModule {

    @Binds
    abstract fun redditClient(redditClient: RedditClientImpl): RedditClient

    @Binds
    abstract fun bindUpdootAccountProvider(accountsProvider: RedditClientImpl): UpdootAccountsProvider

    @Binds
    abstract fun bindUpdootAccountManager(accountsManager: RedditClientImpl): UpdootAccountManager


    companion object {
        @Provides
        @Singleton
        fun provideWorkConfiguration(hiltWorkerFactory: HiltWorkerFactory): Configuration =
            Configuration
                .Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setWorkerFactory(hiltWorkerFactory)
                .build()

        @Provides
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)

        @Singleton
        @Provides
        fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
            SimpleExoPlayer.Builder(context).build()
    }
}