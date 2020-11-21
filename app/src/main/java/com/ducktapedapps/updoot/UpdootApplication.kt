package com.ducktapedapps.updoot

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class UpdootApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workConfiguration: Configuration

    override fun onCreate() {
        super.onCreate()
        setDefaultNightMode(
                Integer.parseInt(getDefaultSharedPreferences(this).getString(getString(R.string.theme_key), MODE_NIGHT_FOLLOW_SYSTEM.toString())
                        ?: MODE_NIGHT_FOLLOW_SYSTEM.toString())
        )
    }

    override fun getWorkManagerConfiguration(): Configuration = workConfiguration
}