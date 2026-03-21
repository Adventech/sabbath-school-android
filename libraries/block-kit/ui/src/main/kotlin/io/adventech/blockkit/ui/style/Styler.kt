/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.adventech.blockkit.model.BlockLevelStyle
import io.adventech.blockkit.model.BlocksStyle
import io.adventech.blockkit.model.SegmentStyle
import io.adventech.blockkit.model.SpacingStyle
import io.adventech.blockkit.model.TextStyleAlignment
import io.adventech.blockkit.ui.color.AttributedTextColorOverride
import io.adventech.blockkit.ui.color.Gray100
import io.adventech.blockkit.ui.color.Gray300
import io.adventech.blockkit.ui.color.Primary800
import io.adventech.blockkit.ui.color.Primary950
import io.adventech.blockkit.ui.color.Sepia300
import io.adventech.blockkit.ui.color.Sepia400
import io.adventech.blockkit.ui.color.parse
import io.adventech.blockkit.ui.style.ReaderStyle.Theme
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import io.adventech.blockkit.model.TextStyle as BlockTextStyle

object Styler {

    @Stable
    @Composable
    fun textColor(
        blockStyle: BlockTextStyle?,
        template: StyleTemplate = BlockStyleTemplate.DEFAULT,
    ): Color {
        val blockStyleColor = blockStyle?.color?.let { Color.parse(it) }
        val color = if (template.themeColorOverride) {
            when (LocalReaderStyle.current.theme) {
                Theme.Light -> blockStyleColor
                Theme.Auto -> blockStyleColor?.takeUnless { isSystemInDarkTheme() }
                else -> null
            }
        } else {
            blockStyleColor
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

    @Stable
    @Composable
    fun textSize(
        blockStyle: BlockTextStyle?,
        template: StyleTemplate = BlockStyleTemplate.DEFAULT,
    ): TextUnit {
        return blockStyle?.size?.let {
            template.textSizePoints(LocalReaderStyle.current.size, it).sp
        } ?: template.textSizeDefault()
    }

    @Stable
    @Composable
    fun textStyle(
        blockStyle: BlockTextStyle?,
        template: StyleTemplate = BlockStyleTemplate.DEFAULT,
    ): TextStyle {
        return TextStyle(
            color = textColor(blockStyle, template),
            fontSize = textSize(blockStyle, template),
            fontFamily = fontFamily(blockStyle, template),
            platformStyle = PlatformTextStyle(includeFontPadding = true)
        )
    }

    @Stable
    @Composable
    private fun fontFamily(blockStyle: BlockTextStyle?, template: StyleTemplate = BlockStyleTemplate.DEFAULT): FontFamily {
        val blocksStyle = LocalBlocksStyle.current
        val typeface = blockStyle?.typeface ?: blocksStyle?.nested?.all?.text?.typeface
        val defaultFontFamily = template.fontFamilyDefault()

        if (typeface == null) {
            return defaultFontFamily
        } else {
            val provider = LocalFontFamilyProvider.current
            val fontFamily by produceState(defaultFontFamily) {
                provider(typeface, defaultFontFamily)
                    .collect { value = it }
            }
            return fontFamily
        }
    }

    @Stable
    @Composable
    fun defaultFontFamily(): FontFamily = fontFamily(LocalReaderStyle.current.typeface)

    fun fontFamily(typeface: ReaderStyle.Typeface): FontFamily = when (typeface) {
        ReaderStyle.Typeface.Andada -> LoraFontFamily
        ReaderStyle.Typeface.Lato -> LatoFontFamily
        ReaderStyle.Typeface.PtSerif -> PTSerifFontFamily
        ReaderStyle.Typeface.PtSans -> PTSansFontFamily
    }

    fun padding(
        style: SpacingStyle?,
        template: StyleTemplate = BlockStyleTemplate.DEFAULT,
    ): PaddingValues {
        return PaddingValues(
            start = template.paddingSizePoints(style?.start).dp,
            top = template.paddingSizePoints(style?.top).dp,
            end = template.paddingSizePoints(style?.end).dp,
            bottom = template.paddingSizePoints(style?.bottom).dp,
        )
    }

    @Stable
    @Composable
    fun backgroundColor(style: BlockLevelStyle?): Color {
        val readerStyle = LocalReaderStyle.current
        val blocksStyle = LocalBlocksStyle.current

        // This block's level style
        style?.backgroundColor?.let {
            return when (readerStyle.theme) {
                Theme.Light -> Color.parse(it)
                Theme.Auto -> {
                    if (isSystemInDarkTheme()) {
                        genericBackgroundColorForInteractiveBlock(readerStyle.theme)
                    } else {
                        Color.parse(it)
                    }
                }

                else -> genericBackgroundColorForInteractiveBlock(readerStyle.theme)
            }
        }

        // Global blocks background color
        blocksStyle?.nested?.all?.block?.backgroundColor?.let {
            return Color.parse(it)
        }

        // Default to no background color
        return Color.Unspecified

    }

    @Stable
    @Composable
    fun genericBackgroundColorForInteractiveBlock(
        theme: Theme = LocalReaderStyle.current.theme,
    ): Color {
        val light = Color.Gray100
        val sepia = Color.Sepia300
        val dark = Color.Primary950
        return themedColor(light, sepia, dark, theme)
    }

    @Stable
    @Composable
    fun genericForegroundColorForInteractiveBlock(
        theme: Theme = LocalReaderStyle.current.theme,
    ): Color {
        val light = Color.Black
        val sepia = Color(0xFF5b4636)
        val dark = Color.White

        return themedColor(light, sepia, dark, theme)
    }

    fun roundedShape() = RoundedCornerShape(6.dp)

    @Stable
    @Composable
    fun borderColor(): Color {
        val light = Color.Gray300
        val sepia = Color.Sepia400
        val dark = Color.Primary800

        return themedColor(light, sepia, dark)
    }

    @Stable
    @Composable
    fun themedColor(
        light: Color,
        sepia: Color,
        dark: Color,
        theme: Theme = LocalReaderStyle.current.theme,
    ): Color {
        return when (theme) {
            Theme.Light -> light
            Theme.Dark -> dark
            Theme.Auto -> if (isSystemInDarkTheme()) dark else light
            Theme.Sepia -> sepia
        }
    }
}

@Composable
internal fun rememberAttributedTextColorOverride(
    color: Color,
    styleTemplate: StyleTemplate,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    theme: Theme = LocalReaderStyle.current.theme,
) = remember(color, styleTemplate, isDarkTheme) {
    if (styleTemplate.themeColorOverride) {
        when (theme) {
            Theme.Light -> AttributedTextColorOverride.None
            Theme.Sepia,
            Theme.Dark -> AttributedTextColorOverride.CustomColor(color)
            Theme.Auto -> if (isDarkTheme) AttributedTextColorOverride.CustomColor(color) else AttributedTextColorOverride.None
        }
    } else {
        AttributedTextColorOverride.None
    }
}


val LocalBlocksStyle = staticCompositionLocalOf<BlocksStyle?> { null }
val LocalSegmentStyle = staticCompositionLocalOf<SegmentStyle?> { null }

internal fun PaddingValues.copy(
    layoutDirection: LayoutDirection,
    start: Dp? = null,
    top: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null,
) = PaddingValues(
    start = start ?: calculateStartPadding(layoutDirection),
    top = top ?: calculateTopPadding(),
    end = end ?: calculateEndPadding(layoutDirection),
    bottom = bottom ?: calculateBottomPadding(),
)
