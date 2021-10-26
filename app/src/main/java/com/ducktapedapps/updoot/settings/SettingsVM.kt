package com.ducktapedapps.updoot.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.comments.CommentPrefManager
import com.ducktapedapps.updoot.common.ThemeManager
import com.ducktapedapps.updoot.utils.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SettingsVM {
    val viewState: StateFlow<ViewState>

    fun setTheme(newTheme: ThemeType)

    fun toggleSingleThreadIndicator()

    fun toggleSingleThreadColor()
}

@HiltViewModel
class SettingsVMImpl @Inject constructor(
    private val themeManager: ThemeManager,
    private val commentPrefsManager: CommentPrefManager,
) : SettingsVM, ViewModel() {
    override val viewState: StateFlow<ViewState> = combine(
        themeManager.themeType(),
        commentPrefsManager.showSingleThreadColor(),
        commentPrefsManager.showSingleThread(),
    ) { currentThemeType, singleThreadColor, singleThread ->
        ViewState(
            themePref = Pair(currentThemeType, ThemeType.values().toList()),
            isSingleColorCommentThreadColorPref = singleThreadColor,
            isSingleThreadComment = singleThread
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, ViewState.getDefaultViewState())

    override fun setTheme(newTheme: ThemeType) {
        viewModelScope.launch { themeManager.setThemeType(newTheme) }
    }

    override fun toggleSingleThreadIndicator() {
        viewModelScope.launch { commentPrefsManager.toggleSingleThread() }
    }

    override fun toggleSingleThreadColor() {
        viewModelScope.launch { commentPrefsManager.toggleSingleThreadColor() }
    }
}

data class ViewState(
    val themePref: Pair<ThemeType, List<ThemeType>>,
    val isSingleColorCommentThreadColorPref: Boolean,
    val isSingleThreadComment: Boolean,
) {
    companion object {
        fun getDefaultViewState() = ViewState(
            themePref = Pair(ThemeType.AUTO, ThemeType.values().toList()),
            isSingleColorCommentThreadColorPref = true,
            isSingleThreadComment = true,
        )
    }
}