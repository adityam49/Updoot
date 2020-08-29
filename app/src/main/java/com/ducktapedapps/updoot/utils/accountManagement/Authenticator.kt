package com.ducktapedapps.updoot.utils.accountManagement

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle

class Authenticator internal constructor(context: Context) : AbstractAccountAuthenticator(context) {
    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle = throw UnsupportedOperationException()

    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String, requiredFeatures: Array<String>, options: Bundle): Bundle? = null

    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle): Bundle? = null

    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle? = null

    override fun getAccountRemovalAllowed(response: AccountAuthenticatorResponse, account: Account) =
            Bundle().apply {
                putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
            }

    override fun getAuthTokenLabel(authTokenType: String): String = throw UnsupportedOperationException()


    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle = throw UnsupportedOperationException()


    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>): Bundle = throw UnsupportedOperationException()

}