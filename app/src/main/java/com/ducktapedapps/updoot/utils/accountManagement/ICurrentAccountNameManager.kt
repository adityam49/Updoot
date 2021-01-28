package com.ducktapedapps.updoot.utils.accountManagement

import kotlinx.coroutines.flow.Flow

interface ICurrentAccountNameManager {

    fun currentAccountName(): Flow<String>

    fun deviceId(): Flow<String>

    suspend fun setCurrentAccountName(user: String)

}