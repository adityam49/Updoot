package com.ducktapedapps.updoot.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.ducktapedapps.updoot.backgroundWork.enqueueCleanUpWork
import com.ducktapedapps.updoot.backgroundWork.enqueueSubscriptionSyncWork
import com.ducktapedapps.updoot.ui.common.IThemeManager
import com.ducktapedapps.updoot.ui.navDrawer.*
import com.ducktapedapps.updoot.utils.ThemeType
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountManager
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ActivityVM @ViewModelInject constructor(
    private val workManager: WorkManager,
    private val updootAccountManager: UpdootAccountManager,
    updootAccountsProvider: UpdootAccountsProvider,
    themeManager: IThemeManager,
    getUserSubscriptionsUseCase: GetUserSubscriptionsUseCase,
) : ViewModel() {
    private val _shouldReload: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val shouldReload: SharedFlow<Boolean> = _shouldReload

    val currentAccount: StateFlow<AccountModel?> = updootAccountsProvider.allAccounts
        .map { it.firstOrNull { account -> account.isCurrent } }
        .onEach { reloadContent() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AnonymousAccount(true))

    val accounts: StateFlow<List<AccountModel>> = updootAccountsProvider.allAccounts

    val theme: StateFlow<ThemeType> = themeManager.themeType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeType.AUTO)

    val subscriptions: StateFlow<List<SubscriptionSubredditUiModel>> =
        getUserSubscriptionsUseCase.subscriptions.map {
            it.map { subreddit -> subreddit.toSubscriptionSubredditUiModel() }
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
        )

    private val _navigationRequest: MutableSharedFlow<NavigationDestination> = MutableSharedFlow()
    val navigationRequest: SharedFlow<NavigationDestination> = _navigationRequest

    fun navigateTo(destination: NavigationDestination) {
        viewModelScope.launch { _navigationRequest.emit(destination) }
    }

    fun setCurrentAccount(name: String) {
        viewModelScope.launch {
            updootAccountManager.setCurrentAccount(name)
        }
    }

    fun reloadContent() {
        viewModelScope.launch { _shouldReload.emit(true) }
    }

    fun logout(accountName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updootAccountManager.removeUser(accountName)
        }
    }

    val navigationEntries: StateFlow<List<NavigationDestination>> = combine(
        flow { emit(AllNavigationEntries) },
        currentAccount
    ) { allEntries, currentAccount ->
        when (currentAccount) {
            is AnonymousAccount -> allEntries.filterNot { it.isUserSpecific }
            is UserModel -> allEntries
            null -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    override fun onCleared() {
        super.onCleared()
        workManager.apply {
            enqueueCleanUpWork()
            enqueueSubscriptionSyncWork()
        }
    }
}