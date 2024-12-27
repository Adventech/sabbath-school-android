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
import androidx.compose.ui.text.style.TextAlign
import io.adventech.blockkit.model.BlockStyle
import io.adventech.blockkit.model.TextStyleAlignment

internal object Styler {

    @Composable
    fun textColor(template: StyleTemplate, blockStyle: BlockStyle?): Color {
        val readerStyle = LocalReaderStyle.current
        val blockStyleColor = blockStyle?.text?.color?.let { Color.parse(it) }
        val color = when (readerStyle.theme) {
            ReaderStyle.Theme.Light -> blockStyleColor
            ReaderStyle.Theme.Auto -> blockStyleColor?.takeUnless { isSystemInDarkTheme() }
            else -> null
        }
        return color ?: template.textColorDefault()
    }

    fun textAlign(blockStyle: BlockStyle?): TextAlign? {
        return when (blockStyle?.text?.align) {
            TextStyleAlignment.START -> TextAlign.Start
            TextStyleAlignment.END -> TextAlign.End
            TextStyleAlignment.CENTER -> TextAlign.Center
            else -> null
        }
    }
}
