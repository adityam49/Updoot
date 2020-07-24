package com.ducktapedapps.updoot.di

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Configuration
import com.ducktapedapps.updoot.backgroundWork.UpdootWorkerFactory
import com.ducktapedapps.updoot.utils.Constants
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.Module
import dagger.Provides
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.linkify.LinkifyPlugin
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)


    @Provides
    @Singleton
    fun provideMarkwon(context: Context): Markwon = Markwon.builder(context)
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
    fun provideWorkConfiguration(updootWorkerFactory: UpdootWorkerFactory): Configuration = Configuration
            .Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(updootWorkerFactory)
            .build()

    @Provides
    @Singleton
    fun provideExoPlayer(context: Context): ExoPlayer = SimpleExoPlayer.Builder(context).build()
}