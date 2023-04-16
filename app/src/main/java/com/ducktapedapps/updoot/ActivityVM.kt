package com.ducktapedapps.updoot

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.NavigationDirections
import com.ducktapedapps.navigation.NavigationDirections.LoginScreenNavigation
import com.ducktapedapps.navigation.NavigationDirections.UserScreenNavigation
import com.ducktapedapps.updoot.MainActivityActions.AddAccount
import com.ducktapedapps.updoot.MainActivityActions.GoToAccount
import com.ducktapedapps.updoot.MainActivityActions.RemoveAccount
import com.ducktapedapps.updoot.MainActivityActions.SendEvent
import com.ducktapedapps.updoot.MainActivityActions.SwitchToAccount
import com.ducktapedapps.updoot.common.ThemeManager
import com.ducktapedapps.updoot.utils.ThemeType
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountManager
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ActivityVM {
    val viewState: StateFlow<ViewState>
    fun doAction(action: MainActivityActions)

    val eventChannel: Channel<Event>
}

@HiltViewModel
class ActivityVMImpl @Inject constructor(
        themeManager: ThemeManager,
        accountsProvider: UpdootAccountsProvider,
        private val accountManager: UpdootAccountManager,
) : ViewModel(), ActivityVM {
    override val eventChannel: Channel<Event> = Channel()

    override val viewState: StateFlow<ViewState> = combine(
            themeManager.themeType(),
            accountsProvider.allAccounts,
    ) { theme, accounts ->
        ViewState(
                theme = theme,
                accounts = accounts,
                bottomNavigationItems = UpdootBottomNavigationItem.getItems().run {
                    val accountFound = accounts.firstOrNull { it.isCurrent }?.asCurrentAccountBottomNavigationItem()
                    if (accountFound != null) {
                        this + accountFound
                    } else {
                        this
                    }
                }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ViewState())

    override fun doAction(action: MainActivityActions) {
        when (action) {
            is RemoveAccount -> removeAccount(action.accountName)
            is SwitchToAccount -> switchToAccount(action.accountName)
            AddAccount -> {
                sendEvent(
                        ScreenNavigationEvent(
                                LoginScreenNavigation.open()
                        )
                )
            }

            is GoToAccount -> {
                sendEvent(
                        ScreenNavigationEvent(
                                UserScreenNavigation.open(
                                        action.accountName
                                )
                        )
                )
            }

            is SendEvent -> sendEvent(action.event)

        }
    }

    private fun removeAccount(accountName: String) {
        viewModelScope.launch { accountManager.removeUser(accountName) }
    }

    private fun switchToAccount(accountName: String) {
        viewModelScope.launch { accountManager.setCurrentAccount(accountName) }
    }

    private fun sendEvent(event: Event) {
        viewModelScope.launch { eventChannel.send(event) }
    }

    private fun AccountModel.asCurrentAccountBottomNavigationItem(): UpdootBottomNavigationItem {
        return when (this) {
            is AnonymousAccount -> UpdootBottomNavigationItem.Accounts(Icons.Outlined.AccountCircle)
            is UserModel -> UpdootBottomNavigationItem.Accounts(userIcon)
        }
    }
}

sealed class MainActivityActions {
    data class RemoveAccount(val accountName: String) : MainActivityActions()

    data class SwitchToAccount(val accountName: String) : MainActivityActions()

    data class GoToAccount(val accountName: String) : MainActivityActions()

    object AddAccount : MainActivityActions()

    data class SendEvent(val event: Event) : MainActivityActions()

}

sealed class UpdootBottomNavigationItem(val icon: Any, val destination: String) {
    object Posts : UpdootBottomNavigationItem(Icons.Outlined.Home, NavigationDirections.SubredditScreenNavigation.destination)
    data class Accounts(private val _icon: Any) :
            UpdootBottomNavigationItem(_icon, NavigationDirections.AccountSelectionNavigation.destination)

    object Search : UpdootBottomNavigationItem(Icons.Outlined.Search, NavigationDirections.SearchNavigation.destination)

    object Settings :
            UpdootBottomNavigationItem(Icons.Outlined.Settings, NavigationDirections.SettingsScreenNavigation.destination)

    companion object {
        fun getItems() = listOf(Posts, Search, Settings)
    }
}


data class ViewState(
        val accounts: List<AccountModel> = emptyList(),
        val theme: ThemeType = ThemeType.AUTO,
        val bottomNavigationItems: List<UpdootBottomNavigationItem> = emptyList()
)