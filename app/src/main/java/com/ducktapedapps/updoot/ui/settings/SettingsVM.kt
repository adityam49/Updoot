package com.ducktapedapps.updoot.ui.settings

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore
import com.ducktapedapps.updoot.utils.ThemeType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsVM @ViewModelInject constructor(
        private val dataStore: UpdootDataStore
) : ViewModel() {
    val theme: StateFlow<ThemeType> = dataStore.themeType()
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeType.AUTO)
    val showSingleThreadIndicator = dataStore.showSingleThread()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showSingleColorThread = dataStore.showSingleThreadColor()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setTheme(theme: ThemeType) {
        viewModelScope.launch { dataStore.setThemeType(theme) }
    }

    fun toggleSingleThreadIndicator() {
        viewModelScope.launch { dataStore.toggleSingleThread() }
    }

    fun toggleSingleThreadColor() {
        viewModelScope.launch { dataStore.toggleSingleThreadColor() }
    }
}