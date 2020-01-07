package com.ducktapedapps.updoot.utils.accountManagement

import android.accounts.AccountManager
import android.util.Log
import com.ducktapedapps.updoot.api.AuthAPI
import com.ducktapedapps.updoot.api.RedditAPI
import com.ducktapedapps.updoot.utils.Constants
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class Reddit @Inject constructor(
        private val redditAPI: RedditAPI,
        private val authAPI: AuthAPI,
        private val interceptor: TokenInterceptor,
        private val userManager: UserManager,
        private val accountManager: AccountManager,
        @Named("device_id") private val deviceId: String
) {
    suspend fun authenticatedAPI(): RedditAPI {
        Log.i(this.javaClass.simpleName, "authenticating api")
        val account = userManager.currentUser
        val expiry = interceptor.tokenExpiry
        if (account == null || (expiry == null || expiry < System.currentTimeMillis()) && account.name == Constants.ANON_USER) {
            //first app launch OR fresh app launch with anonymous Account logged in or userless token expired
            val token = authAPI.getUserLessToken(device_id = deviceId)
            token.setAbsoluteExpiry()
            userManager.setCurrentUser(Constants.ANON_USER, token)
        } else {
            //fresh app start but not the fresh install
            if (expiry == null || expiry < System.currentTimeMillis()) {
                val token = authAPI.getRefreshedToken(refresh_token = accountManager.getUserData(account, Constants.USER_TOKEN_REFRESH_KEY)
                        ?: "".also {
                            throw Exception("saved refresh token is empty ?")
                        })
                token.setAbsoluteExpiry()
                userManager.updateUserSessionData(token)
            }
        }
        return redditAPI
    }
}