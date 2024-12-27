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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.adventech.blockkit.model.TextStyleAlignment
import io.adventech.blockkit.model.TextStyle as BlockTextStyle

object Styler {

    @Composable
    fun textColor(
        blockStyle: BlockTextStyle?,
        template: StyleTemplate = BlockStyleTemplate.DEFAULT,
    ): Color {
        val readerStyle = LocalReaderStyle.current
        val blockStyleColor = blockStyle?.color?.let { Color.parse(it) }
        val color = when (readerStyle.theme) {
            ReaderStyle.Theme.Light -> blockStyleColor
            ReaderStyle.Theme.Auto -> blockStyleColor?.takeUnless { isSystemInDarkTheme() }
            else -> null
        }
        return color ?: template.textColorDefault()
    }

    fun textAlign(textStyle: BlockTextStyle?): TextAlign? {
        return when (textStyle?.align) {
            TextStyleAlignment.START -> TextAlign.Start
            TextStyleAlignment.END -> TextAlign.End
            TextStyleAlignment.CENTER -> TextAlign.Center
            else -> null
        }
    }

    @Composable
    fun textSize(
        blockStyle: BlockTextStyle?,
        template: StyleTemplate = BlockStyleTemplate.DEFAULT,
    ): TextUnit {
        return blockStyle?.size?.let {
            template.textSizePoints(LocalReaderStyle.current.size, it).sp
        } ?: template.textSizeDefault()
    }

    @Composable
    fun textStyle(
        blockStyle: BlockTextStyle?,
        template: StyleTemplate = BlockStyleTemplate.DEFAULT,
    ): TextStyle {
        return TextStyle(
            color = textColor(blockStyle, template),
            fontSize = textSize(blockStyle, template),
            fontFamily = fontFamily(blockStyle),
        )
    }

    private fun fontFamily(blockStyle: BlockTextStyle?): FontFamily {
        return blockStyle?.typeface?.let { typeface ->
            val fontName = GoogleFont(typeface)
            FontFamily(Font(googleFont = fontName, fontProvider = GoogleFontProvider))
        } ?: LatoFontFamily
    }
}
