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

package io.adventech.blockkit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.showBackground
import io.adventech.blockkit.ui.style.thenIf
import io.adventech.blockkit.ui.style.toAlignment

@Composable
internal fun BlockContentWrapper(
    blockItem: BlockItem,
    modifier: Modifier = Modifier,
    blockContent: @Composable (Modifier) -> Unit
) {
    val theme = LocalReaderStyle.current.theme
    val backgroundImage = blockItem.style?.wrapper?.backgroundImage?.takeIf { theme.showBackground() }

    Box(
        modifier = modifier
            .thenIf(blockItem !is BlockItem.Story) {
                height(IntrinsicSize.Min)
            },
    ) {
        backgroundImage?.let {
            AsyncImageBox(
                data = it,
                contentDescription = null,
                modifier = Modifier.wrapContentSize(),
                contentScale = ContentScale.Fit,
                alignment = blockItem.style?.wrapper?.backgroundPosition.toAlignment(),
            )
        }

        blockContent(Modifier)
    }
}
