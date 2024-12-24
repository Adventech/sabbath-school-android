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

package io.adventech.blockkit.model.resource

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.adventech.blockkit.model.AnyBlock

@Keep
@JsonClass(generateAdapter = true)
data class Segment(
    val id: String,
    val index: String,
    val name: String,
    val title: String = "",
    val type: SegmentType = SegmentType.UNKNOWN,
    val resourceId: String = "",
    val markdownTitle: String? = null,
    val subtitle: String? = null,
    val markdownSubtitle: String? = null,
    val titleBelowCover: Boolean? = null,
    val cover: String? = null,
    val blocks: List<AnyBlock>? = null,
    val date: String? = null,
    val background: String? = null
)

@JsonClass(generateAdapter = false)
enum class SegmentChipsStyle {
    UNKNOWN,
    @Json(name = "menu") MENU,
    @Json(name = "carousel") CAROUSEL,
}
