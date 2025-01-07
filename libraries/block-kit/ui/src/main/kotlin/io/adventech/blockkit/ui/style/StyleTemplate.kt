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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.adventech.blockkit.model.BlockStyleSpacing
import io.adventech.blockkit.model.TextStyleSize

interface StyleTemplate {
    @Composable
    fun textSizeDefault(): TextUnit

    @Composable
    fun textColorDefault(): Color

    val textSizePoints: (ReaderStyle.Size, TextStyleSize) -> Float

    val defaultTextSizePoints: (TextStyleSize?) -> Float

    val textTypefaceEnabled: Boolean

    val paddingSizePoints: (BlockStyleSpacing?) -> Int
}

interface BlockStyleTemplate : StyleTemplate {
    @Composable
    override fun textSizeDefault(): TextUnit {
        val config = LocalReaderStyle.current
        return textSizePoints(config.size, TextStyleSize.BASE).sp
    }

    @Composable
    override fun textColorDefault(): Color {
        val config = LocalReaderStyle.current
        return config.theme.primaryForeground()
    }

    override val textTypefaceEnabled: Boolean
        get() = true

    override val defaultTextSizePoints: (TextStyleSize?) -> Float get() = { textStyleSize ->
        textSizePoints(ReaderStyle.Size.Medium, textStyleSize ?: TextStyleSize.BASE)
    }

    override val textSizePoints: (ReaderStyle.Size, TextStyleSize) -> Float
        get() = { size, textStyleSize ->
            val sizeMatrix: Map<ReaderStyle.Size, Map<TextStyleSize, Float>> = mapOf(
                ReaderStyle.Size.Tiny to mapOf(
                    TextStyleSize.XS to 14f,
                    TextStyleSize.SM to 15f,
                    TextStyleSize.BASE to 17f,
                    TextStyleSize.LG to 18f,
                    TextStyleSize.XL to 21f
                ),
                ReaderStyle.Size.Small to mapOf(
                    TextStyleSize.XS to 15f,
                    TextStyleSize.SM to 17f,
                    TextStyleSize.BASE to 18f,
                    TextStyleSize.LG to 21f,
                    TextStyleSize.XL to 24f
                ),
                ReaderStyle.Size.Medium to mapOf(
                    TextStyleSize.XS to 17f,
                    TextStyleSize.SM to 18f,
                    TextStyleSize.BASE to 21f,
                    TextStyleSize.LG to 24f,
                    TextStyleSize.XL to 26f
                ),
                ReaderStyle.Size.Large to mapOf(
                    TextStyleSize.XS to 18f,
                    TextStyleSize.SM to 21f,
                    TextStyleSize.BASE to 24f,
                    TextStyleSize.LG to 26f,
                    TextStyleSize.XL to 28f,
                ),
                ReaderStyle.Size.Huge to mapOf(
                    TextStyleSize.XS to 21f,
                    TextStyleSize.SM to 24f,
                    TextStyleSize.BASE to 26f,
                    TextStyleSize.LG to 28f,
                    TextStyleSize.XL to 30f
                )
            )

            sizeMatrix[size]?.get(textStyleSize) ?: 21f
        }

    override val paddingSizePoints: (BlockStyleSpacing?) -> Int
        get() = { spacing ->
            when (spacing) {
                BlockStyleSpacing.XS -> 4
                BlockStyleSpacing.SM -> 8
                BlockStyleSpacing.BASE -> 16
                BlockStyleSpacing.LG -> 24
                BlockStyleSpacing.XL -> 32
                else -> 0
            }
        }

    companion object {
        val DEFAULT = object : BlockStyleTemplate {}
    }
}

internal data class HeadingStyleTemplate(private val depth: Int) : BlockStyleTemplate {
    override val textSizePoints: (ReaderStyle.Size, TextStyleSize) -> Float
        get() = { size, textStyleSize ->
            val sizeMatrix: Map<ReaderStyle.Size, Map<Int, Float>> = mapOf(
                ReaderStyle.Size.Tiny to mapOf(
                    1 to 24f,
                    2 to 20f,
                    3 to 17f,
                    4 to 15f,
                    5 to 13f,
                    6 to 12f
                ),
                ReaderStyle.Size.Small to mapOf(
                    1 to 28f,
                    2 to 24f,
                    3 to 20f,
                    4 to 17f,
                    5 to 15f,
                    6 to 13f
                ),
                ReaderStyle.Size.Medium to mapOf(
                    1 to 34f,
                    2 to 28f,
                    3 to 24f,
                    4 to 20f,
                    5 to 17f,
                    6 to 15f
                ),
                ReaderStyle.Size.Large to mapOf(
                    1 to 37f,
                    2 to 34f,
                    3 to 28f,
                    4 to 24f,
                    5 to 20f,
                    6 to 17f
                ),
                ReaderStyle.Size.Huge to mapOf(
                    1 to 42f,
                    2 to 38f,
                    3 to 34f,
                    4 to 30f,
                    5 to 24f,
                    6 to 20f
                )
            )

            sizeMatrix[size]?.get(depth) ?: 34f
        }
}
