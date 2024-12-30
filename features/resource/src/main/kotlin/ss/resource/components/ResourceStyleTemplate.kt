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

package ss.resource.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.adventech.blockkit.model.TextStyleSize
import io.adventech.blockkit.ui.style.BlockStyleTemplate
import io.adventech.blockkit.ui.style.ReaderStyle

class ResourceStyleTemplate : BlockStyleTemplate {

    @Composable
    override fun textSizeDefault(): TextUnit = 30.sp

    @Composable
    override fun textColorDefault(): Color = Color.White

    override val textSizePoints: (ReaderStyle.Size, TextStyleSize) -> Float
        get() = { _, size ->
            when (size) {
                TextStyleSize.XS -> 12f
                TextStyleSize.SM -> 14f
                TextStyleSize.BASE -> 16f
                TextStyleSize.LG -> 18f
                TextStyleSize.XL -> 20f
                else -> 0f
            }
        }

    override val defaultTextSizePoints: (TextStyleSize?) -> Float
        get() = { size ->
            when (size) {
                TextStyleSize.XS -> 22f
                TextStyleSize.SM -> 26f
                TextStyleSize.BASE -> 30f
                TextStyleSize.LG -> 34f
                TextStyleSize.XL -> 42f
                else -> 30f
            }
        }
}
