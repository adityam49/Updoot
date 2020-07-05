package com.ducktapedapps.updoot.utils.accountManagement

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ducktapedapps.updoot.ui.login.LoginActivity

class Authenticator internal constructor(private val mContext: Context) : AbstractAccountAuthenticator(mContext) {
    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle = throw UnsupportedOperationException()

    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String, requiredFeatures: Array<String>, options: Bundle): Bundle {
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

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