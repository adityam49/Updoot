package com.ducktapedapps.updoot.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.ModalBottomSheetForActions
import com.ducktapedapps.updoot.settings.SettingScreenAction.ShowThemeMenu
import com.ducktapedapps.updoot.settings.SettingScreenAction.ToggleCommentThreadColorMode
import com.ducktapedapps.updoot.settings.SettingScreenAction.ToggleCommentThreadCount

@Composable
fun SettingsScreen(viewModel: SettingsVM = hiltViewModel<SettingsVMImpl>()) {
    val viewState = viewModel.viewState.collectAsState()
    SettingsScreen(
        viewState = viewState.value,
        doAction = viewModel::doAction
    )
}

@Composable
private fun SettingsScreen(
    viewState: ViewState,
    doAction: (SettingScreenAction) -> Unit,
) {

    var bottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheetForActions(
        bottomSheetVisible = bottomSheetVisible,
        publishEvent = {
            bottomSheetVisible = false
        },
        options = viewState.bottomSheetMenuItems,
        hideBottomSheet = { bottomSheetVisible = false }
    )

    LazyColumn {
        item {
            ThemePreferenceRow(themePrefs = viewState.themePref) {
                doAction(ShowThemeMenu)
                bottomSheetVisible = true
            }
        }
        item {
            CommentThreadColorMode(
                isSingleColorMode = viewState.isSingleColorCommentThreadColorPref,
                toggleMode = { doAction(ToggleCommentThreadColorMode) }
            )
        }
        item {
            CommentThreadModeRow(
                isSingleThreadMode = viewState.isSingleThreadComment,
                toggleMode = { doAction(ToggleCommentThreadCount) }
            )
        }
    }
}

@Composable
fun SettingsRow(
    onClick: () -> Unit,
    title: String,
    subTitle: String,
    controlUiElement: @Composable (() -> Unit)? = null,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable { onClick() }
        .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = subTitle, style = MaterialTheme.typography.labelMedium)
        }
        controlUiElement?.invoke()
    }
}

@Composable
fun CommentThreadColorMode(isSingleColorMode: Boolean, toggleMode: () -> Unit) {
    SettingsRow(
        title = stringResource(id = R.string.using_single_color_thread),
        subTitle = stringResource(
            if (isSingleColorMode) R.string.using_single_color_thread
            else R.string.using_multiple_color_thread
        ),
        onClick = toggleMode,
    ) {
        Switch(checked = isSingleColorMode, onCheckedChange = { toggleMode() })
    }
}

@Composable
fun CommentThreadModeRow(isSingleThreadMode: Boolean, toggleMode: () -> Unit) {
    SettingsRow(
        title = stringResource(id = R.string.single_thread_indicator),
        subTitle = stringResource(
            if (isSingleThreadMode) R.string.using_single_thread
            else R.string.using_multiple_threads
        ),
        onClick = toggleMode,
    ) {
        Switch(checked = isSingleThreadMode, onCheckedChange = { toggleMode() })
    }
}


@Composable
fun ThemePreferenceRow(
    themePrefs: Pair<String, String>,
    showThemeMenu: () -> Unit,
) {
    SettingsRow(
        onClick = showThemeMenu,
        title = themePrefs.first,
        subTitle = themePrefs.second,
    )
}
