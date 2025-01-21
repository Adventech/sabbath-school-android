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

package io.adventech.blockkit.model.adapter

import androidx.annotation.Keep
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import io.adventech.blockkit.model.BlockItem
import java.lang.reflect.Type

/** Moshi [JsonAdapter.Factory] for parsing [BlockItem] items. */
@Keep
class BlockItemJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: Set<Annotation?>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        return PolymorphicJsonAdapterFactory.of(BlockItem::class.java, "type")
            .withSubtype(BlockItem.Appeal::class.java, "appeal")
            .withSubtype(BlockItem.Audio::class.java, "audio")
            .withSubtype(BlockItem.Quote::class.java, "blockquote")
            .withSubtype(BlockItem.Checklist::class.java, "checklist")
            .withSubtype(BlockItem.ChecklistItem::class.java, "list-item-checklist")
            .withSubtype(BlockItem.Collapse::class.java, "collapse")
            .withSubtype(BlockItem.Excerpt::class.java, "excerpt")
            .withSubtype(BlockItem.ExcerptItem::class.java, "excerpt-item")
            .withSubtype(BlockItem.Heading::class.java, "heading")
            .withSubtype(BlockItem.Hr::class.java, "hr")
            .withSubtype(BlockItem.Image::class.java, "image")
            .withSubtype(BlockItem.BlockList::class.java, "list")
            .withSubtype(BlockItem.BlockListItem::class.java, "list-item")
            .withSubtype(BlockItem.MultipleChoice::class.java, "multiple-choice")
            .withSubtype(BlockItem.MultipleChoiceItem::class.java, "list-item-choice")
            .withSubtype(BlockItem.Paragraph::class.java, "paragraph")
            .withSubtype(BlockItem.Poll::class.java, "poll")
            .withSubtype(BlockItem.PollItem::class.java, "poll-item")
            .withSubtype(BlockItem.Reference::class.java, "reference")
            .withSubtype(BlockItem.Story::class.java, "story")
            .withSubtype(BlockItem.StorySlide::class.java, "storySlide")
            .withSubtype(BlockItem.TableBlock::class.java, "table")
            .withSubtype(BlockItem.Question::class.java, "question")
            .withSubtype(BlockItem.Video::class.java, "video")
            .withSubtype(BlockItem.Carousel::class.java, "carousel")
            .withSubtype(BlockItem.Unknown::class.java, "unknown")
            .withDefaultValue(BlockItem.Unknown())
            .create(type, annotations, moshi)
    }

}
