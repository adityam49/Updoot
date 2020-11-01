package com.ducktapedapps.updoot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val lightBlue = Color(0x83b9ff)
val lightBlueVariant = Color(0x3C62CB)
val darkBlue = Color(0x2979FF)
val darkBlueVariant = Color(0x2979FF)
val lightWhiteGray = Color(0xF8F8F8)
val darkWhiteGray = Color(0xDCDCDC)
val lightGray = Color(0x202124)
val darkGray = Color(0xff00ff)
val darkRed = Color(0xF44336)
val lightRed = Color(0xFF7961)

val UpdootLightColors = lightColors(
        primary = darkBlue,
        primaryVariant = darkBlueVariant,
        onPrimary = Color.White,
        surface = lightGray,
        onSurface = Color.Black,
        secondary = Color.White,
        onSecondary = Color.Black,
        error = darkRed,
        onError = Color.White
)


val UpdootDarkColors = darkColors(
        primary = lightBlue,
        primaryVariant = lightBlueVariant,
        onPrimary = Color.Black,
        surface = lightGray,
        onSurface = Color.White,
        secondary = Color.Black,
        onSecondary = Color.White,
        error = lightRed,
        onError = Color.White
)

@Composable
val surfaceOnDrawer: Color
    get() = if (isSystemInDarkTheme()) darkGray else darkWhiteGray
