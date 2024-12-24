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
import io.adventech.blockkit.model.AnyBlock
import io.adventech.blockkit.model.Appeal
import io.adventech.blockkit.model.AudioBlock
import io.adventech.blockkit.model.BlockImage
import io.adventech.blockkit.model.BlockList
import io.adventech.blockkit.model.BlockListItem
import io.adventech.blockkit.model.Blockquote
import io.adventech.blockkit.model.Checklist
import io.adventech.blockkit.model.ChecklistItem
import io.adventech.blockkit.model.Collapse
import io.adventech.blockkit.model.Excerpt
import io.adventech.blockkit.model.ExcerptItem
import io.adventech.blockkit.model.Heading
import io.adventech.blockkit.model.Hr
import io.adventech.blockkit.model.MultipleChoice
import io.adventech.blockkit.model.MultipleChoiceItem
import io.adventech.blockkit.model.Paragraph
import io.adventech.blockkit.model.Poll
import io.adventech.blockkit.model.PollItem
import io.adventech.blockkit.model.Question
import io.adventech.blockkit.model.Reference
import io.adventech.blockkit.model.Story
import io.adventech.blockkit.model.StorySlide
import io.adventech.blockkit.model.TableBlock
import io.adventech.blockkit.model.Unknown
import io.adventech.blockkit.model.VideoBlock
import java.lang.reflect.Type

/** Moshi [JsonAdapter.Factory] for parsing [AnyBlock] items. */
@Keep
class AnyBlockJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: Set<Annotation?>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        return PolymorphicJsonAdapterFactory.of(AnyBlock::class.java, "type")
            .withSubtype(Appeal::class.java, "appeal")
            .withSubtype(AudioBlock::class.java, "audio")
            .withSubtype(Blockquote::class.java, "blockquote")
            .withSubtype(Checklist::class.java, "checklist")
            .withSubtype(ChecklistItem::class.java, "list-item-checklist")
            .withSubtype(Collapse::class.java, "collapse")
            .withSubtype(Excerpt::class.java, "excerpt")
            .withSubtype(ExcerptItem::class.java, "excerpt-item")
            .withSubtype(Heading::class.java, "heading")
            .withSubtype(Hr::class.java, "hr")
            .withSubtype(BlockImage::class.java, "image")
            .withSubtype(BlockList::class.java, "list")
            .withSubtype(BlockListItem::class.java, "list-item")
            .withSubtype(MultipleChoice::class.java, "multiple-choice")
            .withSubtype(MultipleChoiceItem::class.java, "list-item-choice")
            .withSubtype(Paragraph::class.java, "paragraph")
            .withSubtype(Poll::class.java, "poll")
            .withSubtype(PollItem::class.java, "poll-item")
            .withSubtype(Reference::class.java, "reference")
            .withSubtype(Story::class.java, "story")
            .withSubtype(StorySlide::class.java, "storySlide")
            .withSubtype(TableBlock::class.java, "table")
            .withSubtype(Question::class.java, "question")
            .withSubtype(VideoBlock::class.java, "video")
            .withSubtype(Unknown::class.java, "unknown")
            .withDefaultValue(Unknown())
            .create(type, annotations, moshi)
    }

}
