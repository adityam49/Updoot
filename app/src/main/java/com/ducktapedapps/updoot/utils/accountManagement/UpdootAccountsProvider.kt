package com.ducktapedapps.updoot.utils.accountManagement

import kotlinx.coroutines.flow.StateFlow

interface UpdootAccountsProvider {

    val allAccounts: StateFlow<List<AccountModel>>

}