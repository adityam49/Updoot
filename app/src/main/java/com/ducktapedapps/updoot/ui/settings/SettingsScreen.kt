package com.ducktapedapps.updoot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.common.MenuItemModel
import com.ducktapedapps.updoot.ui.common.OptionsDialog
import com.ducktapedapps.updoot.utils.ThemeType
import com.ducktapedapps.updoot.utils.ThemeType.*

@Composable
fun SettingsScreen(viewModel: SettingsVM) {
    val theme = viewModel.theme.collectAsState()
    val isSingleColorMode = viewModel.showSingleColorThread.collectAsState()
    val isSingleThreadMode = viewModel.showSingleThreadIndicator.collectAsState()
    Column {
        ThemePreferenceRow(currentTheme = theme.value, setTheme = viewModel::setTheme)
        CommentThreadColorMode(isSingleColorMode = isSingleColorMode.value, toggleMode = viewModel::toggleSingleThreadColor)
        CommentThreadModeRow(isSingleThreadMode = isSingleThreadMode.value, toggleMode = viewModel::toggleSingleThreadIndicator)
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
            Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
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
fun ThemePreferenceRow(currentTheme: ThemeType, setTheme: (ThemeType) -> Unit) {
    val dialogVisible = remember { mutableStateOf(false) }
    SettingsRow(
            onClick = { dialogVisible.value = true },
            title = stringResource(id = R.string.theme),
            subTitle = stringResource(id = when (currentTheme) {
                DARK -> R.string.dark_theme
                LIGHT -> R.string.light_theme
                AUTO -> R.string.follow_system
            })
    ) {}

    if (dialogVisible.value) {
        val listOfThemes = listOf(
                MenuItemModel(
                        onClick = {
                            dialogVisible.value = false
                            setTheme(DARK)
                        },
                        title = stringResource(id = R.string.dark_theme),
                        icon = R.drawable.ic_theme_icon_24dp,
                ),
                MenuItemModel(
                        onClick = {
                            dialogVisible.value = false
                            setTheme(LIGHT)
                        },
                        title = stringResource(id = R.string.light_theme),
                        icon = R.drawable.ic_theme_icon_24dp,
                ),
                MenuItemModel(
                        onClick = {
                            dialogVisible.value = false
                            setTheme(AUTO)
                        },
                        title = stringResource(id = R.string.follow_system),
                        icon = R.drawable.ic_theme_icon_24dp,
                )

        )
        OptionsDialog(dismiss = { dialogVisible.value = false }, options = listOfThemes)
    }
}