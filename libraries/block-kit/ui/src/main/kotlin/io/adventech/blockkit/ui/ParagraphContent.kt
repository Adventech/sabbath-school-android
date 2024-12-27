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

package io.adventech.blockkit.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.style.BlockStyleTemplate
import io.adventech.blockkit.ui.style.Styler

@Composable
internal fun ParagraphContent(blockItem: BlockItem.Paragraph, modifier: Modifier = Modifier) {
    val style = BlockStyleTemplate.DEFAULT
    val blockStyle = blockItem.style

    MarkdownText(
        markdownText = blockItem.markdown,
        modifier = modifier,
        color = Styler.textColor(style, blockStyle),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = style.textSizeDefault()
        ),
        textAlign = Styler.textAlign(blockStyle),
        onHandleUri = {}
    )
}
