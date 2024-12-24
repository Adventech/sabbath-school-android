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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class BlockType {
    UNKNOWN,
    @Json(name = "appeal") APPEAL,
    @Json(name = "audio") AUDIO,
    @Json(name = "blockquote") BLOCKQUOTE,
    @Json(name = "checklist") CHECKLIST,
    @Json(name = "list-item-checklist") CHECKLIST_ITEM,
    @Json(name = "collapse") COLLAPSE,
    @Json(name = "excerpt") EXCERPT,
    @Json(name = "excerpt-item") EXCERPT_ITEM,
    @Json(name = "heading") HEADING,
    @Json(name = "hr") HR,
    @Json(name = "image") IMAGE,
    @Json(name = "list") LIST,
    @Json(name = "list-item") LIST_ITEM,
    @Json(name = "multiple-choice") MULTIPLE_CHOICE,
    @Json(name = "list-item-choice") MULTIPLE_CHOICE_ITEM,
    @Json(name = "paragraph") PARAGRAPH,
    @Json(name = "poll") POLL,
    @Json(name = "poll-item") POLL_ITEM,
    @Json(name = "reference") REFERENCE,
    @Json(name = "story") STORY,
    @Json(name = "storySlide") STORY_SLIDE,
    @Json(name = "table") TABLE,
    @Json(name = "question") QUESTION,
    @Json(name = "video") VIDEO,
}
