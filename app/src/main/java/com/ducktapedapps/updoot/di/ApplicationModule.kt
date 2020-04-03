package com.ducktapedapps.updoot.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.ducktapedapps.updoot.utils.Constants
import dagger.Module
import dagger.Provides
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule(private val mApplication: Application) {
    @Provides
    @Singleton
    fun application(): Application {
        return mApplication
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return mApplication.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideMarkwon(application: Application): Markwon {
        return Markwon.builder(application)
                .usePlugin(LinkifyPlugin.create())
                .build()
    }

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

}