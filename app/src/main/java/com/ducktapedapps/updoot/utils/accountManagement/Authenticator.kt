package com.ducktapedapps.updoot.utils.accountManagement

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.ducktapedapps.updoot.api.AuthAPI
import com.ducktapedapps.updoot.ui.LoginActivity
import com.ducktapedapps.updoot.utils.Constants
import okhttp3.Credentials
import javax.inject.Inject

class Authenticator internal constructor(private val mContext: Context) : AbstractAccountAuthenticator(mContext) {
    @Inject
    lateinit var authAPI: AuthAPI

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle {
        throw UnsupportedOperationException()
    }

    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String, requiredFeatures: Array<String>, options: Bundle): Bundle {
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle): Bundle? {
        return null
    }

    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle? = null

    override fun getAccountRemovalAllowed(response: AccountAuthenticatorResponse, account: Account): Bundle {
        if (account.name == Constants.ANON_USER) {
            val result = Bundle()
            //restrict anon Account removal from settings
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
            return result
        }
        return super.getAccountRemovalAllowed(response, account)
    }

    override fun getAuthTokenLabel(authTokenType: String): String {
        throw UnsupportedOperationException()
    }

    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {
        throw UnsupportedOperationException()
    }

    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>): Bundle {
        throw UnsupportedOperationException()
    }

    companion object {
        private const val TAG = "Authenticator"
    }

}