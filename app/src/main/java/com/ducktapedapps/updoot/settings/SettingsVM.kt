package com.ducktapedapps.updoot.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.comments.CommentPrefManager
import com.ducktapedapps.updoot.common.BottomSheetItemType
import com.ducktapedapps.updoot.common.MenuItemModel
import com.ducktapedapps.updoot.common.ThemeManager
import com.ducktapedapps.updoot.settings.OptionsBottomSheetEvent.*
import com.ducktapedapps.updoot.settings.SettingScreenAction.ShowThemeMenu
import com.ducktapedapps.updoot.settings.SettingScreenAction.ToggleCommentThreadCount
import com.ducktapedapps.updoot.settings.SettingScreenAction.ToggleCommentThreadColorMode
import com.ducktapedapps.updoot.utils.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import javax.inject.Inject

interface SettingsVM {
    val viewState: StateFlow<ViewState>

    fun doAction(action: SettingScreenAction)
}

@HiltViewModel
class SettingsVMImpl @Inject constructor(
    private val themeManager: ThemeManager,
    private val commentPrefsManager: CommentPrefManager,
) : SettingsVM, ViewModel() {
    private val bottomSheetEventBus: MutableStateFlow<OptionsBottomSheetEvent> = MutableStateFlow(
        Empty
    )

    override fun doAction(action: SettingScreenAction) {
        when (action) {
            ShowThemeMenu -> {
                bottomSheetEventBus.value = ShowThemeSelectionMenu
            }

            ToggleCommentThreadCount -> toggleSingleThreadIndicator()
            ToggleCommentThreadColorMode -> toggleSingleThreadColor()
        }
    }

    override val viewState: StateFlow<ViewState> = combine(
        themeManager.themeType(),
        commentPrefsManager.showSingleThreadColor(),
        commentPrefsManager.showSingleThread(),
        bottomSheetEventBus,
    ) { currentThemeType, singleThreadColor, singleThread, bottomSheetEventValue ->
        ViewState(
            themePref = Pair(
                "Theme", when (currentThemeType) {
                    ThemeType.DARK -> "Dark Theme"
                    ThemeType.LIGHT -> "Light Theme"
                    ThemeType.AUTO -> "Follow System"
                }
            ),
            isSingleColorCommentThreadColorPref = singleThreadColor,
            isSingleThreadComment = singleThread,
            bottomSheetMenuItems = when (bottomSheetEventValue) {
                Empty -> emptyList()
                ShowThemeSelectionMenu -> getThemeMenuItems()
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, ViewState())

    private fun getThemeMenuItems(): List<MenuItemModel> = listOf(
        MenuItemModel(
            title = "Follow System",
            icon = R.drawable.ic_theme_icon_24dp,
            onClick = { setTheme(ThemeType.AUTO);BottomSheetItemType.Action }
        ),
        MenuItemModel(
            title = "Dark Theme",
            icon = R.drawable.ic_theme_icon_24dp,
            onClick = { setTheme(ThemeType.DARK);BottomSheetItemType.Action }
        ),
        MenuItemModel(
            title = "Light Theme",
            icon = R.drawable.ic_theme_icon_24dp,
            onClick = { setTheme(ThemeType.LIGHT);BottomSheetItemType.Action }
        )
    )

    private fun setTheme(newTheme: ThemeType) {
        viewModelScope.launch { themeManager.setThemeType(newTheme) }
    }

    private fun toggleSingleThreadIndicator() {
        viewModelScope.launch { commentPrefsManager.toggleSingleThread() }
    }

    private fun toggleSingleThreadColor() {
        viewModelScope.launch { commentPrefsManager.toggleSingleThreadColor() }
    }
}

sealed class SettingScreenAction {
    object ShowThemeMenu : SettingScreenAction()
    object ToggleCommentThreadColorMode : SettingScreenAction()

    object ToggleCommentThreadCount : SettingScreenAction()
}

private sealed class OptionsBottomSheetEvent {
    object ShowThemeSelectionMenu : OptionsBottomSheetEvent()

    object Empty : OptionsBottomSheetEvent()
}

data class ViewState(
    val themePref: Pair<String, String> = Pair("Theme", "Follow System"),
    val isSingleColorCommentThreadColorPref: Boolean = true,
    val isSingleThreadComment: Boolean = true,
    val bottomSheetMenuItems: List<MenuItemModel> = emptyList()
)