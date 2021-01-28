package com.ducktapedapps.updoot.utils.accountManagement

import com.ducktapedapps.updoot.data.local.model.Token
import com.ducktapedapps.updoot.data.remote.RedditAPI
import kotlinx.coroutines.flow.StateFlow

interface IRedditClient {

    val allAccounts: StateFlow<List<AccountModel>>

    /**
     *  All api calls go should get api service object via this method
     */
    suspend fun api(): RedditAPI

    suspend fun setCurrentAccount(name: String)

    suspend fun createAccount(username: String, icon: String, token: Token)

    /**
     *  Removes user and sets Anonymous account as current
     */
    suspend fun removeUser(accountName: String): Boolean
}