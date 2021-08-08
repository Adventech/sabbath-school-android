package com.cryart.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color.White,
    primaryVariant = BaseBlue,
    secondary = BaseBlue,
    background = Color.Black,
    surface = Color.Black,
    onBackground = BaseGrey1,
    onSurface = BaseGrey1,
    error = BaseRed,
)

private val LightColorPalette = lightColors(
    primary = BaseBlue,
    primaryVariant = BaseBlue,
    secondary = BaseBlue,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BaseGrey3,
    onSurface = BaseGrey3,
    error = BaseRed,
)

@Composable
fun SSTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
