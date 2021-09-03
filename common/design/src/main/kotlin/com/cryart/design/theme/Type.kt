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

package com.cryart.design.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.cryart.design.R

val LatoFontFamily = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_medium, FontWeight.Medium),
    Font(R.font.lato_bold, FontWeight.Bold)
)

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = LatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = LatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    h5 = TextStyle(
        fontFamily = LatoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    )
)

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

private fun textStyle(
    color: Color = Color.Unspecified,
    textSize: TextSize,
    fontWeight: FontWeight,
    textDecoration: TextDecoration? = null,
) = TextStyle(
    color = color,
    fontFamily = LatoFontFamily,
    fontSize = textSize.fontSize,
    lineHeight = textSize.lineHeight,
    fontStyle = FontStyle.Normal,
    fontWeight = fontWeight,
    textDecoration = textDecoration,
)

val LabelMedium = textStyle(
    textSize = TextSize.Size16,
    fontWeight = FontWeight.Medium,
)
val LabelSmall = textStyle(
    textSize = TextSize.Size15,
    fontWeight = FontWeight.Normal,
)
val Body = textStyle(
    textSize = TextSize.Size13,
    fontWeight = FontWeight.Normal,
    color = BaseGrey2
)
val BodyMedium1 = textStyle(
    textSize = TextSize.Size13,
    fontWeight = FontWeight.SemiBold,
    color = BaseGrey2
)
val Title = textStyle(
    textSize = TextSize.Size24,
    fontWeight = FontWeight.Bold,
)
val TitleSmall = Title.copy(
    fontSize = TextSize.Size15.fontSize
)
val TitleMedium = Title.copy(
    fontSize = TextSize.Size18.fontSize
)
