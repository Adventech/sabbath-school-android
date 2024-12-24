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

package io.adventech.blockkit.model

import dev.zacsweers.moshix.adapters.AdaptedBy
import io.adventech.blockkit.model.adapter.BlockItemJsonAdapterFactory
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.Segment
import java.util.UUID

@AdaptedBy(BlockItemJsonAdapterFactory::class)
sealed interface BlockItem {
    val id: String
    val style: BlockStyle?
    val data: BlockData?
    val nested: Boolean?

    data class Appeal(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val markdown: String,
    ) : BlockItem

    data class Audio(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val src: String,
        val caption: String?,
        val credits: AudioBlockCredits?
    ) : BlockItem

    data class Quote(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val memoryVerse: Boolean?,
        val citation: Boolean?,
        val callout: Boolean?,
        val caption: String?,
        val items: List<BlockItem>,
    ) : BlockItem

    data class Checklist(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val ordered: Boolean?,
        val start: Int?,
        val items: List<ChecklistItem>,
    ) : BlockItem

    data class ChecklistItem(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val index: Int,
        val markdown: String,
    ) : BlockItem

    data class Collapse(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val caption: String,
        val items: List<BlockItem>,
    ) : BlockItem

    data class Excerpt(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val options: List<String>,
        val items: List<ExcerptItem>,
    ) : BlockItem

    data class ExcerptItem(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val option: String,
        val items: List<BlockItem>,
    ) : BlockItem

    data class Heading(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val markdown: String,
        val depth: Int,
    ) : BlockItem

    data class Hr(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?
    ) : BlockItem

    data class Image(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val src: String,
        val caption: String?,
    ) : BlockItem

    data class BlockList(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val items: List<BlockListItem>,
        val depth: Int?,
        val ordered: Boolean?,
        val start: Int?,
    ) : BlockItem {
        val bullet: String
            get() {
                val depth = this.depth ?: return "•"
                val bullets = listOf("•", "◦", "▪", "▫", "►", "▻")
                return bullets[(depth - 1) % bullets.size]
            }
    }

    data class BlockListItem(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val index: Int?,
        val markdown: String,
    ) : BlockItem

    data class MultipleChoice(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val ordered: Boolean?,
        val start: Int?,
        val items: List<BlockItem>,
        val answer: Int,
    ) : BlockItem

    data class MultipleChoiceItem(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val index: Int,
        val markdown: String,
    ) : BlockItem

    data class Paragraph(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val markdown: String,
    ) : BlockItem

    data class Poll(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val caption: String,
        val items: List<PollItem>,
    ) : BlockItem

    data class PollItem(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val index: Int,
        val markdown: String,
    ) : BlockItem

    data class Reference(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val segment: Segment?,
        val target: String,
        val scope: ReferenceScope,
        val title: String,
        val subtitle: String?,
        val resource: Resource?,
        val document: ResourceDocument?,
    ) : BlockItem

    data class Story(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val items: List<StorySlide>,
    ) : BlockItem

    data class StorySlide(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val image: String,
        val alignment: ImageStyleTextAlignment,
        val markdown: String,
    ) : BlockItem

    data class TableBlock(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val rows: List<TableRow>,
        val header: List<TableCell>,
    ) : BlockItem

    data class Question(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val markdown: String,
    ) : BlockItem

    data class Video(
        override val id: String,
        override val style: BlockStyle?,
        override val data: BlockData?,
        override val nested: Boolean?,
        val src: String,
        val caption: String?,
    ) : BlockItem

    data object Unknown : BlockItem {
        override var id: String = UUID.randomUUID().toString()
        override var style: BlockStyle? = null
        override var data: BlockData? = null
        override var nested: Boolean? = null
    }
}
