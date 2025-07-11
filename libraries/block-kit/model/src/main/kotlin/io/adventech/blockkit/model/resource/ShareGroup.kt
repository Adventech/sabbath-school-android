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

package io.adventech.blockkit.model.resource

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.adapters.AdaptedBy
import io.adventech.blockkit.model.adapter.ShareGroupJsonAdapterFactory

@Keep
@AdaptedBy(ShareGroupJsonAdapterFactory::class)
sealed interface ShareGroup {
    val title: String
    val selected: Boolean?

    @JsonClass(generateAdapter = true)
    data class Link(
        override val title: String,
        override val selected: Boolean?,
        val links: List<ShareLinkURL>
    ): ShareGroup

    @JsonClass(generateAdapter = true)
    data class File(
        override val title: String,
        override val selected: Boolean?,
        val files: List<ShareFileURL>
    ): ShareGroup

    data object Unknown : ShareGroup {
        override val title: String = "Unknown"
        override val selected: Boolean? = null
    }
}

@Keep
@JsonClass(generateAdapter = true)
data class ShareLinkURL(
    val title: String?,
    val src: String
)

@Keep
@JsonClass(generateAdapter = true)
data class ShareFileURL(
    val title: String?,
    val fileName: String?,
    val src: String
)
