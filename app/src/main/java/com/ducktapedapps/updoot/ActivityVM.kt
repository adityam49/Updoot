package com.ducktapedapps.updoot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.updoot.common.ThemeManager
import com.ducktapedapps.updoot.utils.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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