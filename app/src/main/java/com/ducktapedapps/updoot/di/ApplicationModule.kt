package com.ducktapedapps.updoot.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.ducktapedapps.updoot.utils.Constants
import dagger.Module
import dagger.Provides
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
        return mApplication.getSharedPreferences(Constants.TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
    }

}