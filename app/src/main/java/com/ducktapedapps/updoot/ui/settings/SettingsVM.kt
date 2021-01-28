package com.ducktapedapps.updoot.ui.settings

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.ui.comments.ICommentPrefManager
import com.ducktapedapps.updoot.ui.common.IThemeManager
import com.ducktapedapps.updoot.utils.ThemeType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsVM @ViewModelInject constructor(
        private val themeManager: IThemeManager,
        private val commentPrefsManager: ICommentPrefManager,
) : ViewModel() {
    val theme: StateFlow<ThemeType> = themeManager.themeType()
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeType.AUTO)
    val showSingleThreadIndicator = commentPrefsManager.showSingleThread()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showSingleColorThread = commentPrefsManager.showSingleThreadColor()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setTheme(theme: ThemeType) {
        viewModelScope.launch { themeManager.setThemeType(theme) }
    }

    fun toggleSingleThreadIndicator() {
        viewModelScope.launch { commentPrefsManager.toggleSingleThread() }
    }

    fun toggleSingleThreadColor() {
        viewModelScope.launch { commentPrefsManager.toggleSingleThreadColor() }
    }
}