package com.ducktapedapps.updoot

import android.app.Application
import com.ducktapedapps.updoot.di.ApplicationModule
import com.ducktapedapps.updoot.di.DaggerUpdootComponent
import com.ducktapedapps.updoot.di.UpdootComponent
import com.ducktapedapps.updoot.utils.Constants
import java.util.*

class UpdootApplication : Application() {
    lateinit var updootComponent: UpdootComponent
        private set

    override fun onCreate() {
        super.onCreate()
        updootComponent = DaggerUpdootComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
        createDeviceId()
    }

    private fun createDeviceId() {
        val sharedPreferences = updootComponent.sharedPreferences
        if (sharedPreferences.getString(Constants.DEVICE_ID_KEY, null) == null) {
            sharedPreferences
                    .edit()
                    .putString(Constants.DEVICE_ID_KEY, UUID.randomUUID().toString())
                    .apply()
        }
    }

}