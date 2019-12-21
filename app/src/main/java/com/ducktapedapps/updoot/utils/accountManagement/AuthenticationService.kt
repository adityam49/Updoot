package com.ducktapedapps.updoot.utils.accountManagement

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticationService : Service() {
    private lateinit var mAuthenticator: Authenticator
    override fun onCreate() { // Create a new authenticator object
        mAuthenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mAuthenticator.iBinder
    }
}