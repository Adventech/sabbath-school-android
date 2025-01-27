/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.adventech.blockkit.ui.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

sealed class ReaderStyle {
    enum class Theme(val value: String) {
        Light("light"),
        Sepia("sepia"),
        Dark("dark"),
        Auto("auto");

        companion object {
            fun from(value: String): Theme = entries.associateBy(Theme::value)[value] ?: Auto
        }
    }

    enum class Typeface(val value: String) {
        Andada("andada"),
        Lato("lato"),
        PtSerif("pt-serif"),
        PtSans("pt-sans");

        companion object {
            fun from(value: String): Typeface = entries.associateBy(Typeface::value)[value] ?: Lato
        }
    }

    enum class Size(val value: String) {
        Tiny("tiny"),
        Small("small"),
        Medium("medium"),
        Large("large"),
        Huge("huge");

        companion object {
            fun from(value: String): Size = entries.associateBy(Size::value)[value] ?: Medium
        }
    }
}

@Immutable
data class ReaderStyleConfig(
    val theme: ReaderStyle.Theme = ReaderStyle.Theme.Auto,
    val typeface: ReaderStyle.Typeface = ReaderStyle.Typeface.Lato,
    val size: ReaderStyle.Size = ReaderStyle.Size.Medium
)

val LocalReaderStyle = staticCompositionLocalOf { ReaderStyleConfig() }

@Composable
fun ReaderStyle.Theme.background(): Color {
    return when (this) {
        ReaderStyle.Theme.Light -> Color.White
        ReaderStyle.Theme.Sepia -> Color.Sepia100
        ReaderStyle.Theme.Dark -> Color.Black
        ReaderStyle.Theme.Auto -> if (isSystemInDarkTheme()) Color.Black else Color.White
    }
}

@Composable
fun ReaderStyle.Theme.secondaryBackground(): Color {
    return when (this) {
        ReaderStyle.Theme.Light -> Color.Gray50
        ReaderStyle.Theme.Sepia -> Color.Sepia200
        ReaderStyle.Theme.Dark -> Color.Gray800
        ReaderStyle.Theme.Auto -> if (isSystemInDarkTheme()) Color.Gray800 else Color.Gray50
    }
}

@Composable
fun ReaderStyle.Theme.primaryForeground(): Color {
    return when (this) {
        ReaderStyle.Theme.Light -> Color.Primary950
        ReaderStyle.Theme.Sepia -> Color.Sepia400
        ReaderStyle.Theme.Dark -> Color.White
        ReaderStyle.Theme.Auto -> if (isSystemInDarkTheme()) Color.White else Color.Primary950
    }
}

@Composable
fun ReaderStyle.Theme.secondaryForeground(): Color {
    return when (this) {
        ReaderStyle.Theme.Light -> Color.Gray700
        ReaderStyle.Theme.Sepia -> Color.Sepia400
        ReaderStyle.Theme.Dark -> Color.Primary400
        ReaderStyle.Theme.Auto -> if (isSystemInDarkTheme()) Color.Primary400 else Color.Gray700
    }
}
