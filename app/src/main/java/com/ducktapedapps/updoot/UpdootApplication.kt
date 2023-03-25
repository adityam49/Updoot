package com.ducktapedapps.updoot

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class UpdootApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workConfiguration: Configuration

    override fun getWorkManagerConfiguration(): Configuration = workConfiguration
    override fun onCreate() {
        super.onCreate()
        plantTimberDebugTree()
    }

    private fun plantTimberDebugTree() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}