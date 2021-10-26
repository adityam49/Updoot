package com.ducktapedapps.updoot.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.common.MenuItemModel
import com.ducktapedapps.updoot.common.OptionsDialog
import com.ducktapedapps.updoot.utils.ThemeType
import com.ducktapedapps.updoot.utils.ThemeType.*

@Composable
fun SettingsScreen(viewModel: SettingsVM = hiltViewModel<SettingsVMImpl>()) {
    val viewState = viewModel.viewState.collectAsState()
    SettingsScreen(
        viewState = viewState.value,
        setTheme = viewModel::setTheme,
        toggleSingleThreadColor = viewModel::toggleSingleThreadColor,
        toggleSingleThreadIndicator = viewModel::toggleSingleThreadIndicator
    )
}

@Composable
private fun SettingsScreen(
    viewState: ViewState,
    setTheme: (ThemeType) -> Unit,
    toggleSingleThreadColor: () -> Unit,
    toggleSingleThreadIndicator: () -> Unit
) {
    Column {
        ThemePreferenceRow(themePrefs = viewState.themePref, setTheme)
        CommentThreadColorMode(
            isSingleColorMode = viewState.isSingleColorCommentThreadColorPref,
            toggleMode = toggleSingleThreadColor
        )
        CommentThreadModeRow(
            isSingleThreadMode = viewState.isSingleThreadComment,
            toggleMode = toggleSingleThreadIndicator
        )
    }
}

@Composable
fun SettingsRow(
    onClick: () -> Unit,
    title: String,
    subTitle: String,
    controlUiElement: @Composable () -> Unit,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable { onClick() }
        .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(text = title, style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.padding(8.dp))
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                Text(text = subTitle, style = MaterialTheme.typography.subtitle2)
            }
        }

        controlUiElement()
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
    themePrefs: Pair<ThemeType, List<ThemeType>>,
    setTheme: (ThemeType) -> Unit
) {
    val dialogVisible = remember { mutableStateOf(false) }
    SettingsRow(
        onClick = { dialogVisible.value = true },
        title = stringResource(id = R.string.theme),
        subTitle = stringResource(
            id = when (themePrefs.first) {
                DARK -> R.string.dark_theme
                LIGHT -> R.string.light_theme
                AUTO -> R.string.follow_system
            }
        )
    ) {}

    if (dialogVisible.value) {
        val listOfThemes = themePrefs
            .second
            .map {
                MenuItemModel(
                    onClick = {
                        dialogVisible.value = false
                        setTheme(it)
                    },
                    title = stringResource(
                        id = when (it) {
                            DARK -> R.string.dark_theme
                            LIGHT -> R.string.light_theme
                            AUTO -> R.string.follow_system
                        }
                    ),
                    icon = R.drawable.ic_theme_icon_24dp,
                )
            }
        OptionsDialog(dismiss = { dialogVisible.value = false }, options = listOfThemes)
    }
}