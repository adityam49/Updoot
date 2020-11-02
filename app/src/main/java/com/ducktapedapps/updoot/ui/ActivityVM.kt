package com.ducktapedapps.updoot.ui

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.backgroundWork.enqueueCleanUpWork
import com.ducktapedapps.updoot.backgroundWork.enqueueOneOffSubscriptionsSyncFor
import com.ducktapedapps.updoot.backgroundWork.enqueueSubscriptionSyncWork
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.ui.User.LoggedIn
import com.ducktapedapps.updoot.ui.User.LoggedOut
import com.ducktapedapps.updoot.ui.navDrawer.AccountModel
import com.ducktapedapps.updoot.ui.navDrawer.AllNavigationEntries
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination.*
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityVM(
        private val application: UpdootApplication,
        val redditClient: IRedditClient,
        val subredditDAO: SubredditDAO
) : AndroidViewModel(application) {
    private val _shouldReload = MutableStateFlow(SingleLiveEvent(false))
    val shouldReload: StateFlow<SingleLiveEvent<Boolean>> = _shouldReload

    private val _accounts: MutableStateFlow<List<AccountModel>> = MutableStateFlow(redditClient.getAccountModels())
    private val accountEntriesExpanded = MutableStateFlow(false)

    private val _navDrawerVisible = MutableLiveData(true)
    val navDrawerVisibility = _navDrawerVisible

    private val _currentNavDrawerScreen: MutableState<NavigationDestination> = mutableStateOf(NavigationMenu)
    val currentNavDrawerScreen: State<NavigationDestination> = _currentNavDrawerScreen

    /**
     * Returns false if navigation menu is visible
     * and true if other nested screen is visible
     */
    fun drawerScreenCanGoBack(): Boolean =
            if (currentNavDrawerScreen.value == NavigationMenu) false
            else {
                _currentNavDrawerScreen.value = NavigationMenu
                true
            }

    fun drawerNavigateTo(destination: NavigationDestination) {
        when (destination) {
            NavigationMenu -> _currentNavDrawerScreen.value = NavigationMenu
            Search -> _currentNavDrawerScreen.value = Search
            Explore -> _currentNavDrawerScreen.value = Explore
            CreatePost,
            Inbox,
            History -> Log.e("ActivityViewModel", "${destination.title} Not implemented! ")

            Exit -> TODO()
        }
    }

    val user: Flow<User> = _accounts.map {
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

    val navigationEntries: Flow<List<NavigationDestination>> = combine(flow { emit(AllNavigationEntries) }, user) { allEntries, loginState ->
        when (loginState) {
            is LoggedOut -> allEntries.filterNot { it.isUserSpecific }
            is LoggedIn -> allEntries
        }
    }


    fun enqueueSubscriptionSyncWork() {
        val currentLoggedIn = _accounts.value.first()
        application.enqueueOneOffSubscriptionsSyncFor(currentLoggedIn.name)
    }

    private fun enqueuePeriodicWork() {
        application.apply {
            enqueueSubscriptionSyncWork()
            enqueueCleanUpWork()
        }
    }

    override fun onCleared() {
        super.onCleared()
        enqueuePeriodicWork()
    }
}

sealed class User(val name: String) {
    object LoggedOut : User(Constants.ANON_USER)
    data class LoggedIn(val userName: String) : User(userName)
}

class ActivityVMFactory @Inject constructor(
        private val redditClient: RedditClient,
        private val subredditDAO: SubredditDAO,
        private val application: UpdootApplication
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            ActivityVM(application, redditClient, subredditDAO) as T
}