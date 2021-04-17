package com.ducktapedapps.updoot.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun UpdootTheme(
        isDarkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
) {
    MaterialTheme(
            colors = if (isDarkTheme) UpdootDarkColors else UpdootLightColors,
            typography = UpdootTypography,
            content = content
    )
}
