package com.ducktapedapps.updoot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.ScreenNavigationCommand
import com.ducktapedapps.updoot.backgroundWork.enqueueCleanUpWork
import com.ducktapedapps.updoot.backgroundWork.enqueueSubscriptionSyncWork
import com.ducktapedapps.updoot.common.ThemeManager
import com.ducktapedapps.updoot.navDrawer.*
import com.ducktapedapps.updoot.utils.ThemeType
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountManager
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityVM @Inject constructor(
    themeManager: ThemeManager,
) : ViewModel() {
    val theme: StateFlow<ThemeType> = themeManager.themeType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeType.AUTO)

    val eventChannel: Channel<Event> = Channel()

    fun sendEvent(event: Event) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}