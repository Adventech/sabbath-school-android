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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.style.background

@Composable
fun BlockContent(
    blockItem: BlockItem,
    modifier: Modifier = Modifier,
    nested: Boolean? = blockItem.nested,
    parent: BlockItem? = null,
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
) {
    val blockModifier = modifier.background(blockItem, nested)

    when (blockItem) {
        is BlockItem.Appeal -> {
            AppealContent(blockItem, blockModifier)
        }
        is BlockItem.Audio -> {
            AudioContent(blockItem, blockModifier)
        }
        is BlockItem.BlockList -> {
            BlockListContent(blockItem, blockModifier, onHandleUri)
        }
        is BlockItem.BlockListItem -> {
            BlockListItemContent(blockItem, blockModifier, (parent as? BlockItem.BlockList)?.bullet ?: "") {
                onHandleUri(it, blockItem.data)
            }
        }
        is BlockItem.Checklist -> Unit
        is BlockItem.ChecklistItem -> Unit
        is BlockItem.Collapse -> {
            CollapseContent(blockItem, blockModifier, onHandleUri)
        }
        is BlockItem.Excerpt -> {
            ExcerptContent(blockItem, blockModifier)
        }
        is BlockItem.ExcerptItem -> {
            ExcerptItemContent(blockItem, blockModifier)
        }
        is BlockItem.Heading -> {
            HeadingContent(blockItem, blockModifier) {
                onHandleUri(it, blockItem.data)
            }
        }
        is BlockItem.Hr -> {
            HrContent(blockItem, blockModifier)
        }
        is BlockItem.Image -> {
            ImageContent(blockItem, blockModifier)
        }
        is BlockItem.MultipleChoice -> Unit
        is BlockItem.MultipleChoiceItem -> Unit
        is BlockItem.Paragraph -> {
            ParagraphContent(blockItem, blockModifier) {
                onHandleUri(it, blockItem.data)
            }
        }
        is BlockItem.Poll -> Unit
        is BlockItem.PollItem -> Unit
        is BlockItem.Question -> {
            QuestionContent(blockItem, blockModifier) {
                onHandleUri(it, blockItem.data)
            }
        }
        is BlockItem.Quote -> {
            QuoteContent(blockItem, blockModifier, onHandleUri = onHandleUri)
        }
        is BlockItem.Reference -> {
            ReferenceContent(blockItem, blockModifier)
        }
        is BlockItem.Story -> Unit
        is BlockItem.StorySlide -> Unit
        is BlockItem.TableBlock -> Unit
        is BlockItem.Video -> {
            VideoContent(blockItem, blockModifier)
        }
        is BlockItem.Carousel -> Unit
        is BlockItem.Unknown -> Unit
    }
}
