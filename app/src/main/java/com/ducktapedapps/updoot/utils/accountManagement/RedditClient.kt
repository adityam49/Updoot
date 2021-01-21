package com.ducktapedapps.updoot.utils.accountManagement

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.ducktapedapps.updoot.data.local.model.Token
import com.ducktapedapps.updoot.data.remote.AuthAPI
import com.ducktapedapps.updoot.data.remote.RedditAPI
import com.ducktapedapps.updoot.ui.navDrawer.AccountModel
import com.ducktapedapps.updoot.ui.navDrawer.AccountModel.*
import com.ducktapedapps.updoot.utils.Constants
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

//  TODO:expose current account as sharedFlow
//  TODO:all accounts as Stateflow
//  TODO:ditch sharedPrefs
//  TODO:account manager should use callback flow and ditch the account change listener interface
@Singleton
class RedditClient @Inject constructor(
        private val redditAPI: RedditAPI,
        private val authAPI: AuthAPI,
        private val interceptor: TokenInterceptor,
        private val androidAccountManager: AccountManager,
        private val sharedPreferences: SharedPreferences,
        @Named("device_id")
        private val deviceId: String
) : IRedditClient {
    init {
        checkAndCreateAnonAccount()
        androidAccountManager.addOnAccountsUpdatedListener({
            notifyAccountChange(it)
        }, null, true)
    }

    private var startupFlag = true

    private fun notifyAccountChange(updatedAccounts: Array<Account>) {
        val currentAccountCountInSharedPrefs = sharedPreferences.getInt(Constants.CACHED_ACCOUNT_COUNT, 1)
        if (updatedAccounts.isNotEmpty()) {
            val accountAdded = updatedAccounts.size > currentAccountCountInSharedPrefs
            if (accountAdded &&
                    !startupFlag) {
                listener?.currentAccountChanged()
            } else {
                startupFlag = false
            }
        }
        sharedPreferences.edit().putInt(Constants.CACHED_ACCOUNT_COUNT, updatedAccounts.size).apply()
    }


    private fun checkAndCreateAnonAccount() {
        if (androidAccountManager.accounts.none { it.name == Constants.ANON_USER }) {
            androidAccountManager.addAccountExplicitly(Account(Constants.ANON_USER, Constants.UPDOOT_ACCOUNT), null, null)
            sharedPreferences.edit().apply {
                putString(Constants.CURRENT_ACCOUNT_NAME, Constants.ANON_USER)
                putInt(Constants.CACHED_ACCOUNT_COUNT, 1)
                apply()
            }
        }
    }

    private var listener: IRedditClient.AccountChangeListener? = null

    override fun attachListener(context: Context) {
        listener = context as IRedditClient.AccountChangeListener
    }

    override fun detachListener() {
        listener = null
    }

    private var token: Token? = null

    override suspend fun api(): RedditAPI {
        token = token.run {
            if (isExpiredOrInvalid()) {
                val currentAccount = currentUpdootAccount
                if (currentAccount == Constants.ANON_USER) {
                    authAPI.getUserLessToken(device_id = deviceId).apply {
                        setAbsoluteExpiry()
                        interceptor.sessionToken = access_token
                    }
                } else {
                    authAPI.getRefreshedToken(refresh_token = getAccountRefreshToken(currentAccount)).apply {
                        setAbsoluteExpiry()
                        interceptor.sessionToken = access_token
                    }
                }
            } else
                this
        }
        return redditAPI
    }

    private fun Token?.isExpiredOrInvalid() =
            this == null || this.absoluteExpiry < System.currentTimeMillis()

    private fun invalidateToken() {
        token = null
    }

    override fun createUserAccountAndSetItAsCurrent(username: String, icon: String, token: Token) {
        sharedPreferences.edit().putString(Constants.CURRENT_ACCOUNT_NAME, username).apply()
        androidAccountManager
                .addAccountExplicitly(
                        Account(username, Constants.UPDOOT_ACCOUNT),
                        null,
                        Bundle().apply {
                            putString(Constants.USER_TOKEN_REFRESH_KEY, token.refresh_token)
                            putString(Constants.USER_ICON_KEY, icon)
                        })
    }

    private val currentUpdootAccount: String
        get() {
            val currentAccountInSharedPrefs = sharedPreferences.getString(Constants.CURRENT_ACCOUNT_NAME, null)
                    ?: Constants.ANON_USER
            return androidAccountManager
                    .accounts
                    .filter { it.name == currentAccountInSharedPrefs }
                    .map { it.name }
                    .first()
        }

    private fun getAccountRefreshToken(accountName: String): String =
            with(androidAccountManager) {
                getUserData(accounts.first { it.name == accountName }, Constants.USER_TOKEN_REFRESH_KEY)
            }


    @Throws(RuntimeException::class)
    override fun setCurrentAccount(name: String) {
        with(androidAccountManager) {
            if (accounts.any { it.name == name }) {
                sharedPreferences.edit().putString(Constants.CURRENT_ACCOUNT_NAME, name).apply()
                invalidateToken()
            } else
                throw RuntimeException("Account $name not found")
        }
    }

    private fun getCachedAccount(): List<String> =
            androidAccountManager
                    .accounts
                    .filter { it.name != currentUpdootAccount }
                    .map { it.name }

    override fun getAccountModels(): List<AccountModel> =
            mutableListOf<Pair<String, Boolean>>().apply {
                add(Pair(currentUpdootAccount,/* is current account */true))
                getCachedAccount().forEach { add(Pair(it, false)) }
                add(Pair(Constants.ADD_ACCOUNT, false))
            }.map { it.toAccountModel() }

    private fun Pair<String, Boolean>.toAccountModel(): AccountModel = when (this.first) {
        Constants.ANON_USER -> AnonymousAccount(this.second)
        Constants.ADD_ACCOUNT -> AddAccount
        else -> UserModel(this.first, this.second, with(androidAccountManager) {
            getUserData(accounts.first { it.name == this@toAccountModel.first }, Constants.USER_ICON_KEY)
        })
    }

    override suspend fun removeUser(accountName: String): Boolean {
        val accountToRemove = androidAccountManager.accounts.firstOrNull { it.name == accountName }
        return if (accountToRemove == null) {
            Log.e(TAG, "removeUser: unable to remove account $accountName as it does not exist in system")
            false
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val result = authAPI.logout(refresh_token = androidAccountManager.getUserData(accountToRemove, Constants.USER_TOKEN_REFRESH_KEY))
                if (result.code() == 204) {
                    invalidateToken()
                    if (sharedPreferences.getString(Constants.CURRENT_ACCOUNT_NAME, null) == accountName) {
                        sharedPreferences.edit().putString(Constants.CURRENT_ACCOUNT_NAME, Constants.ANON_USER).apply()
                    }
                    androidAccountManager.removeAccountExplicitly(accountToRemove)
                    true
                } else {
                    //TODO : API level 21 account removal code?
                    false
                }
            } else false
        }
    }


    companion object {
        private const val TAG = "RedditClient"
    }
}