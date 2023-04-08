package com.ducktapedapps.updoot.theme

import android.app.Activity import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MaterialTheme.isLightTheme() = colorScheme.background.luminance() > 0.5

@Composable
fun UpdootTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor =
                    if (isDarkTheme) UpdootDarkColors.surface.toArgb() else UpdootLightColors.surface.toArgb()
                window.navigationBarColor =
                    if (isDarkTheme) UpdootDarkColors.surface.toArgb() else UpdootLightColors.surface.toArgb()

                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !isDarkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                    !isDarkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (isDarkTheme) UpdootDarkColors else UpdootLightColors,
        typography = UpdootTypography,
        content = content
    )
}
