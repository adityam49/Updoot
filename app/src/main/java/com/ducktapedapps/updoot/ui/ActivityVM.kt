package com.ducktapedapps.updoot.ui

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.backgroundWork.enqueueCleanUpWork
import com.ducktapedapps.updoot.backgroundWork.enqueueOneOffSubscriptionsSyncFor
import com.ducktapedapps.updoot.backgroundWork.enqueueSubscriptionSyncWork
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.ui.common.IThemeManager
import com.ducktapedapps.updoot.ui.navDrawer.AllNavigationEntries
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination
import com.ducktapedapps.updoot.utils.ThemeType
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ActivityVM @ViewModelInject constructor(
        @ApplicationContext private val appContext: Context,
        private val redditClient: IRedditClient,
        private val subredditDAO: SubredditDAO,
        themeManager: IThemeManager,
) : ViewModel() {
    private val _shouldReload: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val shouldReload: SharedFlow<Boolean> = _shouldReload

    val currentAccount: StateFlow<AccountModel?> = redditClient.allAccounts
            .map { it.firstOrNull { account -> account.isCurrent } }
            .onEach { reloadContent() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val accounts: StateFlow<List<AccountModel>> = redditClient.allAccounts

    val theme: StateFlow<ThemeType> = themeManager.themeType()
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeType.AUTO)

    fun setCurrentAccount(name: String) {
        viewModelScope.launch {
            redditClient.setCurrentAccount(name)
        }
    }

    fun reloadContent() {
        viewModelScope.launch { _shouldReload.emit(true) }
    }

    fun logout(accountName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            redditClient.removeUser(accountName)
        }
    }

    val navigationEntries: Flow<List<NavigationDestination>> = combine(
            flow { emit(AllNavigationEntries) },
            currentAccount
    ) { allEntries, currentAccount ->
        when (currentAccount) {
            is AnonymousAccount -> allEntries.filterNot { it.isUserSpecific }
            is UserModel -> allEntries
            null -> emptyList()
        }
    }

    fun enqueueSubscriptionSyncWork() {
        viewModelScope.launch {
            currentAccount.value?.name?.let {
                appContext.enqueueOneOffSubscriptionsSyncFor(it)
            }
        }
    }

    private fun enqueuePeriodicWork() {
        appContext.apply {
            enqueueSubscriptionSyncWork()
            enqueueCleanUpWork()
        }
    }

    override fun onCleared() {
        super.onCleared()
        enqueuePeriodicWork()
    }
}