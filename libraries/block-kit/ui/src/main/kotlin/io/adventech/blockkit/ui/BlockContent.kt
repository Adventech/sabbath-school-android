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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.style.BlockStyleTemplate
import io.adventech.blockkit.ui.style.HeadingStyleTemplate

@Composable
fun BlockContent(blockItem: BlockItem, modifier: Modifier = Modifier) {
    when (blockItem) {
        is BlockItem.Appeal -> Unit
        is BlockItem.Audio -> Unit
        is BlockItem.BlockList -> Unit
        is BlockItem.BlockListItem -> Unit
        is BlockItem.Checklist -> Unit
        is BlockItem.ChecklistItem -> Unit
        is BlockItem.Collapse -> Unit
        is BlockItem.Excerpt -> Unit
        is BlockItem.ExcerptItem -> Unit
        is BlockItem.Heading -> {
            val style = HeadingStyleTemplate(blockItem.depth)
            Text(
                text = blockItem.markdown,
                modifier = modifier,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = style.textSizeDefault()
                ),
                color = style.textColorDefault()
            )
        }

        is BlockItem.Hr -> {
            val style = BlockStyleTemplate.DEFAULT
            val color = style.textColorDefault().copy(alpha = 0.7f)
            HorizontalDivider(
                modifier = modifier.padding(vertical = 4.dp),
                color = color,
            )
        }

        is BlockItem.Image -> Unit
        is BlockItem.MultipleChoice -> Unit
        is BlockItem.MultipleChoiceItem -> Unit
        is BlockItem.Paragraph -> {
            val style = BlockStyleTemplate.DEFAULT
            MarkdownText(
                markdownText = blockItem.markdown,
                modifier = modifier,
                color = style.textColorDefault(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = style.textSizeDefault()
                ),
                onHandleUri = {}
            )
        }

        is BlockItem.Poll -> Unit
        is BlockItem.PollItem -> Unit
        is BlockItem.Question -> Unit
        is BlockItem.Quote -> {
            val style = BlockStyleTemplate.DEFAULT
            val color = style.textColorDefault()
            Column(
                modifier = modifier
                    .drawBehind {
                        drawLine(
                            color = color,
                            strokeWidth = 10f,
                            start = Offset(12.dp.value, 0f),
                            end = Offset(12.dp.value, size.height),
                            cap = StrokeCap.Round
                        )
                    }
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                blockItem.items.forEach { BlockContent(it) }
            }
        }

        is BlockItem.Reference -> Unit
        is BlockItem.Story -> Unit
        is BlockItem.StorySlide -> Unit
        is BlockItem.TableBlock -> Unit
        is BlockItem.Video -> Unit
        BlockItem.Unknown -> Unit
    }
}
