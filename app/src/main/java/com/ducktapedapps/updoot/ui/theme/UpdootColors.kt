package com.ducktapedapps.updoot.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

val UpdootLightColors = lightColors(
        primary = "#2979FF".toColor(),
        onPrimary = Color.Black,
        primaryVariant = "#42A5F5".toColor(),
        secondary = Color.White,
        onSecondary = Color.Black,
        error = "#F44336".toColor(),
        onError = Color.White
)



val UpdootDarkColors = darkColors(
        primary = "#83b9ff".toColor(),
        onPrimary = Color.White,
        primaryVariant = "#3C62CB".toColor(),
        secondary = "#2A2B2E".toColor(),
        onSecondary = Color.White,
        onSurface = Color.White,
        error = "#FF7961".toColor(),
        onError = Color.White
)

private fun String.toColor(): Color = Color(android.graphics.Color.parseColor(this))
