package com.ducktapedapps.updoot.utils.accountManagement

import com.ducktapedapps.updoot.data.remote.model.Token
import java.util.Date

interface UpdootAccountManager {

    suspend fun setCurrentAccount(name: String)

    suspend fun createAccount(username: String, icon: String, token: Token)

    suspend fun setLastSyncedAt(timeStamp: Date,username: String)
    /**
     *  Removes user and sets Anonymous account as current
     */
    suspend fun removeUser(accountName: String): Boolean

}