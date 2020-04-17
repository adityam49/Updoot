package com.ducktapedapps.updoot

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.ducktapedapps.updoot.di.ApplicationModule
import com.ducktapedapps.updoot.di.DaggerUpdootComponent
import com.ducktapedapps.updoot.di.UpdootComponent
import com.ducktapedapps.updoot.utils.Constants.THEME_KEY

class UpdootApplication : Application() {
    lateinit var updootComponent: UpdootComponent
        private set

    override fun onCreate() {
        super.onCreate()
        updootComponent = DaggerUpdootComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
        setDefaultNightMode(
                Integer.parseInt(getDefaultSharedPreferences(this).getString(THEME_KEY, MODE_NIGHT_FOLLOW_SYSTEM.toString())
                        ?: MODE_NIGHT_FOLLOW_SYSTEM.toString())
        )
    }
}