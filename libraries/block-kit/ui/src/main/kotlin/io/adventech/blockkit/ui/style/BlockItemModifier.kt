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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockItem

@Composable
internal fun Modifier.background(blockItem: BlockItem, nested: Boolean? = blockItem.nested): Modifier {
    val layoutDirection = LocalLayoutDirection.current
    val blocksStyle = LocalBlocksStyle.current
    val blockStyle = blockItem.style?.block
    val blockPaddingStyle = blockStyle?.padding

    val defaultWrapperPaddingStyle = blocksStyle?.inline?.all?.wrapper?.padding?.takeUnless {
        blockPaddingStyle != null || (blockItem.nested ?: nested) == true
    }

    val stylePaddingValues = Styler.padding(blockPaddingStyle ?: defaultWrapperPaddingStyle)

    return this
        .padding(
            // If the style comes with vertical paddings then remove size already applied in LazyColumn
            stylePaddingValues.copy(
                layoutDirection = layoutDirection,
                top = if (stylePaddingValues.calculateTopPadding() >= 16.dp) {
                    stylePaddingValues.calculateTopPadding() - 16.dp
                } else {
                    stylePaddingValues.calculateTopPadding()
                },
                bottom = if (stylePaddingValues.calculateBottomPadding() >= 16.dp) {
                    stylePaddingValues.calculateBottomPadding() - 16.dp
                } else {
                    stylePaddingValues.calculateBottomPadding()
                }
            )
        )
        .background(
            color = Styler.backgroundColor(blockStyle),
            shape = if (blockStyle?.rounded == true) Styler.roundedShape() else RectangleShape,
        )
        .padding(Styler.padding(blockPaddingStyle))
}

/**
 * Conditionally applies the [builder] block if [condition].
 */
inline fun Modifier.thenIf(
    condition: Boolean,
    builder: Modifier.() -> Modifier
) = if (condition) builder() else this
