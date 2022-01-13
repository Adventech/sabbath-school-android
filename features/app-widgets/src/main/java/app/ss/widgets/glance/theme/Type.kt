/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package app.ss.widgets.glance.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

private sealed class TextSize(
    val fontSize: TextUnit,
    val lineHeight: TextUnit,
) {
    object Size10 : TextSize(10.sp, 14.sp)
    object Size12 : TextSize(12.sp, 18.sp)
    object Size13 : TextSize(13.sp, 20.sp)
    object Size15 : TextSize(15.sp, 22.sp)
    object Size16 : TextSize(16.sp, 24.sp)
    object Size18 : TextSize(18.sp, 26.sp)
    object Size24 : TextSize(24.sp, 28.sp)
    object Size32 : TextSize(32.sp, 40.sp)
}

internal fun TextStyle.copy(
    color: ColorProvider? = this.color,
    fontSize: TextUnit? = this.fontSize,
    fontWeight: FontWeight? = this.fontWeight,
    fontStyle: FontStyle? = this.fontStyle,
    textAlign: TextAlign? = this.textAlign,
    textDecoration: TextDecoration? = this.textDecoration,
) = TextStyle(
    color = color,
    fontSize = fontSize,
    fontWeight = fontWeight,
    fontStyle = fontStyle,
    textAlign = textAlign,
    textDecoration = textDecoration
)

private fun textStyle(
    color: Color,
    textSize: TextSize,
    fontWeight: FontWeight,
    textDecoration: TextDecoration? = null,
) = TextStyle(
    color = ColorProvider(color),
    fontSize = textSize.fontSize,
    fontStyle = FontStyle.Normal,
    fontWeight = fontWeight,
    textDecoration = textDecoration,
)

@Composable
fun todayTitle(
    color: Color? = null
) = textStyle(
    color = color ?: MaterialTheme.colorScheme.onSurface,
    textSize = TextSize.Size18,
    fontWeight = FontWeight.Bold
)

@Composable
fun todayBody(
    color: Color? = null
) = textStyle(
    color = color ?: MaterialTheme.colorScheme.onSurfaceVariant,
    textSize = TextSize.Size15,
    fontWeight = FontWeight.Normal
)
