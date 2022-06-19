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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

val BaseBlue = Color(0xFF2E5797)
val BaseGrey1 = Color(0xFFEFEFEF)
val BaseGrey2 = Color(0xFF8F8E94)
val BaseGrey3 = Color(0xFF606060)
val BaseGrey4 = Color(0xFF383838)
val BaseGrey5 = Color(0xFF1A1A1A)
val BaseRed = Color(0xFFF1706B)
val OffWhite = Color(0xFFE2E2E5)

fun Color.darker(componentDelta: Float = 0.1f): Color = makeColor(-1 * componentDelta)

fun Color.lighter(componentDelta: Float = 0.1f): Color = makeColor(componentDelta)

fun Color.Companion.parse(colorString: String): Color =
    Color(color = android.graphics.Color.parseColor(colorString))

/**
 * Create a new [Color] modifying each component
 * by componentDelta, making it either lighter or darker
 */
fun Color.makeColor(componentDelta: Float): Color = Color(
    red.add(componentDelta),
    green.add(componentDelta),
    blue.add(componentDelta),
    alpha
)

private fun Float.add(toComponent: Float): Float {
    return max(0f, min(1f, toComponent + this))
}

@Composable
@Stable
fun iconTint(
    isDark: Boolean = isSystemInDarkTheme()
): Color {
    return when {
        isDark -> Color.White
        else -> BaseGrey2.lighter(0.2f)
    }
}
