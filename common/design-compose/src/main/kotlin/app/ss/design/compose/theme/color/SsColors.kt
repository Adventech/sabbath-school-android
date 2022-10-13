/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.design.compose.theme.color

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import app.ss.design.compose.extensions.color.darker
import app.ss.design.compose.extensions.color.lighter
import app.ss.design.compose.extensions.isS

@Immutable
class SsColors(
    isDark: Boolean,
    dividers: Color,
    icons: Color,
    navTitle: Color,

    onSurfaceSecondary: Color,
) {
    var isDark by mutableStateOf(isDark)
        private set
    var dividers by mutableStateOf(dividers)
        private set
    var icons by mutableStateOf(icons)
        private set
    var navTitle by mutableStateOf(navTitle)
        private set
    var onSurfaceSecondary by mutableStateOf(onSurfaceSecondary)
        private set

    companion object {
        val BaseBlue = Color(0xFF2E5797)
        val BaseGrey1 = Color(0xFFEFEFEF)
        val BaseGrey2 = Color(0xFF8F8E94)
        val BaseGrey3 = Color(0xFF606060)
        val BaseGrey4 = Color(0xFF383838)
        val BaseGrey5 = Color(0xFF1A1A1A)
        val BaseRed = Color(0xFFF1706B)

        val OffWhite = Color(0xFFE2E2E5)
        val White70 = Color(0xB3FFFFFF)
        val TextLink = Color(0xFF94BDFD)

        internal fun light(colorScheme: ColorScheme) = SsColors(
            isDark = false,
            dividers = Color(0x80D7D7D7),
            icons = BaseGrey2.lighter(0.2f),
            navTitle = if (isS()) colorScheme.onSurface else Color.Black,
            onSurfaceSecondary = colorScheme.onSurface.lighter(0.3f)
        )

        internal fun dark(colorScheme: ColorScheme) = SsColors(
            isDark = true,
            dividers = Color(0x1FFFFFFF),
            icons = colorScheme.primary,
            navTitle = if (isS()) colorScheme.onSurface else Color.White,
            onSurfaceSecondary = colorScheme.onSurface.darker(0.3f)
        )
    }
}

internal val LocalSsColors = staticCompositionLocalOf {
    SsColors(
        isDark = false,
        dividers = Color.Unspecified,
        icons = Color.Unspecified,
        navTitle = Color.Unspecified,
        onSurfaceSecondary = Color.Unspecified
    )
}

val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = SsColors.BaseBlue,
    background = Color.Black,
    surface = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = SsColors.BaseRed
)

val LightColorScheme = lightColorScheme(
    primary = SsColors.BaseBlue,
    secondary = SsColors.BaseBlue,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SsColors.BaseGrey3,
    onSurface = SsColors.BaseGrey3,
    error = SsColors.BaseRed
)

@Composable
internal fun ProvideSsColors(
    ssColors: SsColors,
    content: @Composable () -> Unit
) {
    val colors = remember { ssColors }
    CompositionLocalProvider(LocalSsColors provides colors, content = content)
}
