package com.ducktapedapps.updoot

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.work.Configuration
import com.ducktapedapps.updoot.di.DaggerUpdootComponent
import com.ducktapedapps.updoot.di.UpdootComponent
import com.ducktapedapps.updoot.utils.Constants.THEME_KEY
import javax.inject.Inject

class UpdootApplication : Application(), Configuration.Provider {

    val updootComponent: UpdootComponent by lazy {
        DaggerUpdootComponent.factory().create(this)
    }

    @Inject
    lateinit var workConfiguration: Configuration

    override fun onCreate() {
        super.onCreate()
        updootComponent.inject(this)
        setDefaultNightMode(
                Integer.parseInt(getDefaultSharedPreferences(this).getString(THEME_KEY, MODE_NIGHT_FOLLOW_SYSTEM.toString())
                        ?: MODE_NIGHT_FOLLOW_SYSTEM.toString())
        )
    }

    override fun getWorkManagerConfiguration(): Configuration = workConfiguration

}