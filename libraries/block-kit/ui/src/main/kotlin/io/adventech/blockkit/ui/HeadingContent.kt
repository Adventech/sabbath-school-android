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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.input.rememberContentHighlights
import io.adventech.blockkit.ui.style.HeadingStyleTemplate
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme

@Composable
internal fun HeadingContent(
    blockItem: BlockItem.Heading,
    modifier: Modifier = Modifier,
    inputState: UserInputState? = null,
    onHandleUri: (String) -> Unit = {},
) {
    val template = HeadingStyleTemplate(blockItem.depth)
    val blockStyle = blockItem.style?.text
    val highlights = rememberContentHighlights(blockItem.id, inputState)

    MarkdownText(
        markdownText = blockItem.markdown,
        modifier = modifier,
        style = Styler.textStyle(blockStyle, template).copy(fontWeight = FontWeight.Bold),
        textAlign = Styler.textAlign(blockStyle),
        onHandleUri = onHandleUri,
        highlights = highlights,
    )
}

@PreviewLightDark
@Composable
private fun Preview() {
    BlocksPreviewTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeadingContent(
                    blockItem = BlockItem.Heading(
                        id = "heading",
                        style = null,
                        data = null,
                        nested = null,
                        markdown = "Heading",
                        depth = 1,
                    ),
                )

                HeadingContent(
                    blockItem = BlockItem.Heading(
                        id = "heading-two",
                        style = null,
                        data = null,
                        nested = null,
                        markdown = "2. Heading",
                        depth = 1,
                    ),
                )
            }
        }
    }
}
