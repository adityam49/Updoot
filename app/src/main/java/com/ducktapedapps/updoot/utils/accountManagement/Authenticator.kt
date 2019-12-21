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
import io.reactivex.schedulers.Schedulers
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

    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {
        val am = AccountManager.get(mContext)
        var authToken = am.peekAuthToken(account, authTokenType)
        if (TextUtils.isEmpty(authToken)) {
            authToken = authAPI
                    .getUserToken(Constants.TOKEN_ACCESS_URL,
                            Credentials.basic(Constants.client_id, ""),
                            Constants.user_grantType,
                            options.getString("code") ?: "", Constants.redirect_uri)
                    .subscribeOn(Schedulers.io())
                    .doOnError { throwable: Throwable? -> Log.e(TAG, "getAuthToken: ", throwable) }
                    .blockingGet()
                    .access_token
        }
        if (!TextUtils.isEmpty(authToken)) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return result
        }
        // If you reach here, person needs to login again. or sign up
        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity which is the AccountsActivity in my case.
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra(Constants.ACCOUNT_TYPE, account.type)
        intent.putExtra("full_access", authTokenType)
        val retBundle = Bundle()
        retBundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return retBundle
    }

    @Throws(NetworkErrorException::class)
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