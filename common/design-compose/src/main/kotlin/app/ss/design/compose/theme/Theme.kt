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

package app.ss.design.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.ss.design.compose.extensions.isS
import app.ss.design.compose.theme.color.DarkColorScheme
import app.ss.design.compose.theme.color.LightColorScheme
import app.ss.design.compose.theme.color.LocalSsColors
import app.ss.design.compose.theme.color.ProvideSsColors
import app.ss.design.compose.theme.color.SsColors

@Composable
fun SsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    windowWidthSizeClass: WindowWidthSizeClass? = WindowWidthSizeClass.Compact,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isS() -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val dimensions = when (windowWidthSizeClass) {
        WindowWidthSizeClass.Medium -> sw600Dimensions
        else -> smallDimensions
    }

    ProvideDimens(dimensions = dimensions) {
        ProvideSsColors(
            ssColors = if (darkTheme) SsColors.dark(colorScheme) else SsColors.light(colorScheme)
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = SsTypography,
                content = content
            )
        }
    }
}

object SsTheme {
    val dimens: Dimensions
        @Composable
        get() = LocalAppDimens.current

    val colors: SsColors
        @Composable
        get() = LocalSsColors.current
}
