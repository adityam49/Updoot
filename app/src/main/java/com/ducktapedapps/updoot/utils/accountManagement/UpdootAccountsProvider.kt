package com.ducktapedapps.updoot.utils.accountManagement

import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

interface UpdootAccountsProvider {

    val allAccounts: StateFlow<List<AccountModel>>

    fun getLoggedInUsers(): Flow<List<UserModel>> = allAccounts.map {
        it.filterIsInstance(UserModel::class.java)
    }

    fun getCurrentAccount(): Flow<AccountModel> = allAccounts.transform {
        if (it.any { account -> account.isCurrent }) emit(it.first { account -> account.isCurrent })
    }

    fun isLoggedInUser(): Flow<UserModel?> = getCurrentAccount().map {
        if (it is UserModel) it else null
    }
}