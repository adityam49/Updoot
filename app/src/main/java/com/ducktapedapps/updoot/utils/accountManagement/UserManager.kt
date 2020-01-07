package com.ducktapedapps.updoot.utils.accountManagement

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
        private val accountManager: AccountManager,
        private val sharedPreferences: SharedPreferences,
        private val interceptor: TokenInterceptor
) {
    var listener: AccountChangeListener? = null

    val currentUser: Account?
        get() {
            val currentCachedUser = sharedPreferences.getString(Constants.LOGIN_STATE, null)
            if (currentCachedUser != null) {
                for (account in accountManager.accounts) {
                    if (account.name == currentCachedUser) {
                        return account
                    }
                }
            }
            return null
        }

    fun setCurrentUser(userName: String?, token: Token?) {
        Log.i(TAG, "setCurrentUser: user name is $userName token is $token")
        sharedPreferences.edit().putString(Constants.LOGIN_STATE, userName).apply()
        for (account in accountManager.accounts) {
            if (account.name == userName) {
                interceptor.setSessionToken(token)
                return
            }
        }
        Log.i(TAG, "setCurrentUser: after fresh install")
        interceptor.setSessionToken(token)
        accountManager.addAccountExplicitly(Account(Constants.ANON_USER, Constants.ACCOUNT_TYPE), null, null)
    }

    fun updateUserSessionData(token: Token?) {
        interceptor.setSessionToken(token)
    }

    fun attachListener(context: Context?) {
        listener = context as AccountChangeListener
    }

    fun detachListener() {
        listener = null
    }

    interface AccountChangeListener {
        fun onCurrentAccountRemoved()
    }

    companion object {
        private const val TAG = "UserManager"
    }

}