/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.widgets.glance.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceTheme
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

private fun textStyle(
    color: ColorProvider,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    textDecoration: TextDecoration? = null
) = TextStyle(
    color = color,
    fontSize = fontSize,
    fontStyle = FontStyle.Normal,
    fontWeight = fontWeight,
    textDecoration = textDecoration
)

@Composable
fun todayTitle(
    color: ColorProvider? = null
) = textStyle(
    color = color ?: GlanceTheme.colors.onSurface,
    fontSize = 17.sp,
    fontWeight = FontWeight.Bold
)

@Composable
fun todayBody(
    color: ColorProvider? = null
) = textStyle(
    color = color ?: GlanceTheme.colors.onSurfaceVariant,
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal
)
