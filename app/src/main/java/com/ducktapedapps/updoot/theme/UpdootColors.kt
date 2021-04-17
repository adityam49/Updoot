package com.ducktapedapps.updoot.theme

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// reddit colors
val upVoteColor = Color(0xFFFF8B60)
val downVoteColor = Color(0xFF9494FF)
val saveColor = Color(0xFFFFB100)


val darkGray = Color(0xFF171717)
val lightBlue = Color(0xFF83b9ff)
val lightBlueVariant = Color(0xFF3C62CB)
val darkBlue = Color(0xFF2979FF)
val darkBlueVariant = Color(0xFF2979FF)
val lightGray = Color(0xFF363636)
val darkRed = Color(0xFFF44336)
val lightRed = Color(0xFFFF7961)

val UpdootLightColors = lightColors(
        primary = darkBlue,
        primaryVariant = darkBlueVariant,
        onPrimary = Color.White,
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
        background = darkGray,
        secondary = Color.Black,
        onSecondary = Color.White,
        error = lightRed,
        onError = Color.White
)


val @Composable Colors.BottomDrawerColor: Color
        get() = UpdootDarkColors.surface


val @Composable Colors.SurfaceOnDrawer: Color
        get() = Color(0xFF4F4F4F)


val @Composable Colors.ScoreBackground: Color
        get() = if (isLight) Color(0xFFffd700) else Color(0xFFf9d276)


val @Composable Colors.ColorOnScoreBackground: Color
        get() = Color.Black


val @Composable Colors.StickyPostColor: Color
        get() = if (isLight) Color(0xFF388E3C) else Color(0xFF69F0AE)

val LightThreadColors = arrayOf(
        Color(0xFF7b1fa2),
        Color(0xFF303f9f),
        Color(0xFF1976d2),
        Color(0xFF388e3c),
        Color(0xFF689f38),
        Color(0xFFfbc02d),
        Color(0xFFf57c00),
        Color(0xFFe64a19),
        Color(0xFFc2185b),
        Color(0xFFd32f2f),
)

val DarkThreadColors = arrayOf(
        Color(0xFFb39ddb),
        Color(0xFF9fa8da),
        Color(0xFF90caf9),
        Color(0xFFaed581),
        Color(0xFFdce775),
        Color(0xFFfff59d),
        Color(0xFFffcc80),
        Color(0xFFffab91),
        Color(0xFFf48fb1),
        Color(0xFFef9a9a),
)