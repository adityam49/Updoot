package com.ducktapedapps.updoot.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

@Composable
fun MaterialTheme.isLightTheme() = colorScheme.background.luminance() > 0.5

@Composable
fun UpdootTheme(
        isDarkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
) {
    MaterialTheme(
            colorScheme = if (isDarkTheme) UpdootDarkColors else UpdootLightColors,
            typography = UpdootTypography,
            content = content
    )
}
