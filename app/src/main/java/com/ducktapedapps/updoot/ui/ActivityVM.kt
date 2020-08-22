package com.ducktapedapps.updoot.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.ui.LoginState.LoggedIn
import com.ducktapedapps.updoot.ui.LoginState.LoggedOut
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountModel
import com.ducktapedapps.updoot.ui.navDrawer.destinations.NavDrawerItemModel
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ActivityVM(private val redditClient: IRedditClient, private val subredditDAO: SubredditDAO) : ViewModel() {
    private val _shouldReload = MutableStateFlow(SingleLiveEvent(false))
    val shouldReload: StateFlow<SingleLiveEvent<Boolean>> = _shouldReload

    private val _accounts: MutableStateFlow<List<AccountModel>> = MutableStateFlow(redditClient.getAccountModels())
    private val accountEntriesExpanded = MutableStateFlow(false)

    private val _navDrawerVisible = MutableLiveData<Boolean>(true)
    val navDrawerVisibility = _navDrawerVisible

    val loginState: Flow<LoginState> = _accounts.map {
        if (it.first().name == Constants.ANON_USER) LoggedOut
        else LoggedIn(it.first().name)
    }

    val accounts: Flow<List<AccountModel>> = accountEntriesExpanded.combine(_accounts) { expanded, accounts: List<AccountModel> ->
        if (expanded) accounts
        else listOf(accounts.first())
    }

    private fun reloadAccountList() {
        _accounts.value = redditClient.getAccountModels()
    }

    fun setCurrentAccount(name: String) {
        redditClient.setCurrentAccount(name)
        _shouldReload.value = SingleLiveEvent(true)
        reloadAccountList()
        collapseAccountsMenu()
    }

    fun reloadContent() {
        _shouldReload.value = SingleLiveEvent(true)
        reloadAccountList()
        collapseAccountsMenu()
    }

    fun logout(accountName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val accountRemovedSuccessfully = redditClient.removeUser(accountName)
            if (accountRemovedSuccessfully) withContext(Dispatchers.Main) {
                reloadContent()
                reloadAccountList()
                collapseAccountsMenu()
            }
        }
    }

    private fun collapseAccountsMenu() {
        accountEntriesExpanded.value = false
    }

    fun toggleAccountsMenuList() {
        accountEntriesExpanded.value = !accountEntriesExpanded.value
    }

    fun showBottomNavDrawer() {
        _navDrawerVisible.value = true
    }

    fun hideBottomNavDrawer() {
        _navDrawerVisible.value = false
    }

    val navigationEntries: Flow<List<NavDrawerItemModel>> = loginState.map { account ->
        mutableListOf<NavDrawerItemModel>().apply {
            add(NavDrawerItemModel("Explore", R.drawable.ic_explore_24dp))
            if (account is LoggedIn) {
                add(NavDrawerItemModel("Create Post", R.drawable.ic_baseline_edit_24))
                add(NavDrawerItemModel("Inbox", R.drawable.ic_baseline_inbox_24))
                add(NavDrawerItemModel("Profile", R.drawable.ic_account_circle_24dp))
            }
            add(NavDrawerItemModel("History", R.drawable.ic_baseline_history_24))
        }
    }
}

sealed class LoginState {
    object LoggedOut : LoginState()
    data class LoggedIn(val userName: String) : LoginState()
}

@ExperimentalCoroutinesApi
class ActivityVMFactory @Inject constructor(
        private val redditClient: RedditClient,
        private val subredditDAO: SubredditDAO
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = ActivityVM(redditClient, subredditDAO) as T
}