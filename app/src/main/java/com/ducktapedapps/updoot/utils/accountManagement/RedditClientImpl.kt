package com.ducktapedapps.updoot.utils.accountManagement

import android.accounts.AccountManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.remote.AuthAPI
import com.ducktapedapps.updoot.data.remote.RedditAPI
import com.ducktapedapps.updoot.data.remote.model.Token
import com.ducktapedapps.updoot.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditClientImpl @Inject constructor(
    private val androidAccountManager: AccountManager,
    private val currentAccountNameManager: CurrentAccountNameManager,
    private val tokenInterceptor: TokenInterceptor,
    private val redditAPI: RedditAPI,
    private val authAPI: AuthAPI,
) : RedditClient, UpdootAccountsProvider, UpdootAccountManager {

    private val _allAccounts: MutableStateFlow<List<android.accounts.Account>> =
        MutableStateFlow(emptyList())
    override val allAccounts: StateFlow<List<AccountModel>> = combine(
        _allAccounts,
        currentAccountNameManager.currentAccountName()
    ) { accounts, currentAccountName ->
        if (accounts.isEmpty()) {
            createAnonAccount()
            emptyList()
        } else accounts.map {
            when (it.name) {
                Constants.ANON_USER -> AccountModel.AnonymousAccount(it.name == currentAccountName)
                else -> AccountModel.UserModel(
                    it.name,
                    it.name == currentAccountName,
                    androidAccountManager.getUserData(it, Constants.USER_ICON_KEY)
                )
            }
        }
    }.transform { accounts ->
        if (accounts.isNotEmpty())
            emit(
                mutableListOf<AccountModel>().apply {
                    add(accounts.first { it.isCurrent })
                    addAll(accounts.filterNot { it.isCurrent })
                }
            )

    }.stateIn(GlobalScope, SharingStarted.Eagerly, emptyList())

    init {
        listenToAccountChanges()
    }

    override suspend fun api(): RedditAPI {
        if (currentAccountToken.isExpiredOrInvalid()) {
            val currentAccount = allAccounts.first().firstOrNull { it.isCurrent }
            if (currentAccount != null) {
                currentAccountToken = when (val name = currentAccount.name) {
                    Constants.ANON_USER -> getUserLessToken()
                    else -> getUserToken(name)
                }
                tokenInterceptor.sessionToken = currentAccountToken?.access_token
            } else throw RuntimeException("current account null")
        }
        return redditAPI
    }

    override suspend fun createAccount(username: String, icon: String, token: Token) {
        androidAccountManager
            .addAccountExplicitly(
                android.accounts.Account(username, Constants.UPDOOT_ACCOUNT),
                null,
                Bundle().apply {
                    putString(Constants.USER_TOKEN_REFRESH_KEY, token.refresh_token)
                    putString(Constants.USER_ICON_KEY, icon)
                })
        currentAccountNameManager.setCurrentAccountName(username)
    }

    override suspend fun setCurrentAccount(name: String) {
        with(androidAccountManager) {
            if (accounts.any { it.name == name }) {
                currentAccountNameManager.setCurrentAccountName(name)
                invalidateToken()
            } else
                throw IllegalArgumentException("Account $name not found")
        }
    }

    override suspend fun removeUser(accountName: String): Boolean {
        val accountToRemove = androidAccountManager.accounts.firstOrNull { it.name == accountName }
        return if (accountToRemove == null) {
            Log.e(
                "RedditClient",
                "removeUser: unable to remove account $accountName as it does not exist in system"
            )
            false
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val result = authAPI.logout(
                    refresh_token = androidAccountManager.getUserData(
                        accountToRemove,
                        Constants.USER_TOKEN_REFRESH_KEY
                    )
                )
                if (result.code() in 200..299) {
                    invalidateToken()
                    currentAccountNameManager.setCurrentAccountName(Constants.ANON_USER)
                    androidAccountManager.removeAccountExplicitly(accountToRemove)
                    true
                } else {
                    //TODO : API level 21 account removal code?
                    false
                }
            } else false
        }
    }

    private var currentAccountToken: Token? = null

    private fun invalidateToken() {
        currentAccountToken = null
    }

    private suspend fun getUserToken(username: String): Token = withContext(Dispatchers.IO) {
        authAPI.getRefreshedToken(refresh_token = getAccountRefreshToken(username)).apply {
            setAbsoluteExpiry()
        }
    }


    private suspend fun getUserLessToken(): Token = withContext(Dispatchers.IO) {
        authAPI.getUserLessToken(
            device_id = currentAccountNameManager.deviceId().first()
        ).apply {
            setAbsoluteExpiry()
        }
    }

    private fun listenToAccountChanges() {
        androidAccountManager.addOnAccountsUpdatedListener(
            { accounts -> _allAccounts.value = accounts.toList() },
            null,
            true,
        )
    }

    private suspend fun createAnonAccount() {
        with(androidAccountManager) {
            if (accounts.none { it.name == Constants.ANON_USER }) {
                addAccountExplicitly(
                    android.accounts.Account(
                        Constants.ANON_USER,
                        Constants.UPDOOT_ACCOUNT
                    ), null, null
                )
                currentAccountNameManager.setCurrentAccountName(Constants.ANON_USER)
            }
        }
    }

    private fun getAccountRefreshToken(accountName: String): String =
        with(androidAccountManager) {
            getUserData(accounts.first { it.name == accountName }, Constants.USER_TOKEN_REFRESH_KEY)
        }

    companion object {
        private fun Token?.isExpiredOrInvalid() =
            this == null || this.absoluteExpiry < System.currentTimeMillis()
    }
}

sealed class AccountModel(
    val name: String,
    open val isCurrent: Boolean,
) {
    data class AnonymousAccount(
        override val isCurrent: Boolean,
    ) : AccountModel(Constants.ANON_USER, isCurrent) {
        @DrawableRes
        val icon = R.drawable.ic_account_circle_24dp
    }

    data class UserModel(
        private val _name: String,
        override val isCurrent: Boolean,
        val userIcon: String,
    ) : AccountModel(_name, isCurrent)
}