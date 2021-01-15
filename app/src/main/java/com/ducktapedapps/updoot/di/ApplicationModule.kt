package com.ducktapedapps.updoot.di

import android.content.Context
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorkerFactory
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import androidx.work.WorkManager
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.linkify.LinkifyPlugin
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ApplicationModule {

    @Binds
    abstract fun redditClient(redditClient: RedditClient): IRedditClient

    companion object {
        @Provides
        fun providePrefsManager(@ApplicationContext context: Context): SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)

        @Provides
        @Singleton
        fun provideMarkwon(@ApplicationContext context: Context): Markwon = Markwon.builder(context)
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .build()


        @Provides
        @Named("device_id")
        fun provideDeviceId(sharedPreferences: SharedPreferences): String {
            var id = sharedPreferences.getString(Constants.DEVICE_ID_KEY, null)
            if (id == null) {
                id = UUID.randomUUID().toString()
                sharedPreferences
                        .edit()
                        .putString(Constants.DEVICE_ID_KEY, id)
                        .apply()
            }
            return id
        }

        @Provides
        @Singleton
        fun provideWorkConfiguration(hiltWorkerFactory: HiltWorkerFactory): Configuration = Configuration
                .Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setWorkerFactory(hiltWorkerFactory)
                .build()

        @Provides
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

        @Singleton
        @Provides
        fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer = SimpleExoPlayer.Builder(context).build()
    }
}