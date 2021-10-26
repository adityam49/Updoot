package com.ducktapedapps.updoot.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ducktapedapps.updoot.subreddit.*
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountManager
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

interface AccountsVM {
    val viewState: StateFlow<ViewState>

    fun removeAccount(accountName: String)

    fun switchToAccount(accountName: String)
}

@HiltViewModel
class AccountsVMImpl @Inject constructor(
    accountsProvider: UpdootAccountsProvider,
    private val accountManager: UpdootAccountManager,
) :  ViewModel(),AccountsVM {

    override val viewState: StateFlow<ViewState> = accountsProvider.allAccounts.map {
        ViewState(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly, ViewState.getDefaultViewState()
    )

    override fun removeAccount(accountName: String) {
        viewModelScope.launch { accountManager.removeUser(accountName) }
    }

    override fun switchToAccount(accountName: String) {
        viewModelScope.launch { accountManager.setCurrentAccount(accountName) }
    }

}

data class ViewState(
    val accounts: List<AccountModel>,
) {
    companion object {
        fun getDefaultViewState() = ViewState(emptyList())
    }
}