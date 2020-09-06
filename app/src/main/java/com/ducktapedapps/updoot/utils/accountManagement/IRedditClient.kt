package com.ducktapedapps.updoot.utils.accountManagement

import android.content.Context
import com.ducktapedapps.updoot.api.remote.RedditAPI
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.ui.navDrawer.AccountModel

interface IRedditClient {
    fun attachListener(context: Context)
    fun detachListener()

    suspend fun api(): RedditAPI
    fun createUserAccountAndSetItAsCurrent(username: String, icon: String, token: Token)

    @Throws(RuntimeException::class)
    fun setCurrentAccount(name: String)
    fun getAccountModels(): List<AccountModel>

    suspend fun removeUser(accountName: String): Boolean
    interface AccountChangeListener {
        fun currentAccountChanged()
    }
}