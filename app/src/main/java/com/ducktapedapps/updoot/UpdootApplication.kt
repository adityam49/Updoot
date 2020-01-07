package com.ducktapedapps.updoot

import android.app.Application
import com.ducktapedapps.updoot.di.ApplicationModule
import com.ducktapedapps.updoot.di.DaggerUpdootComponent
import com.ducktapedapps.updoot.di.UpdootComponent

class UpdootApplication : Application() {
    lateinit var updootComponent: UpdootComponent
        private set

    override fun onCreate() {
        super.onCreate()
        updootComponent = DaggerUpdootComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }
}