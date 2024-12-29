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

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class TextStyleAlignment {
    @Json(name = "start") START,
    @Json(name = "end") END,
    @Json(name = "center") CENTER,
    UNKNOWN
}

@JsonClass(generateAdapter = false)
enum class TextStyleSize {
    @Json(name = "xs") XS,
    @Json(name = "sm") SM,
    @Json(name = "base") BASE,
    @Json(name = "lg") LG,
    @Json(name = "xl") XL,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): TextStyleSize {
            return when (value) {
                "xs" -> XS
                "sm" -> SM
                "base" -> BASE
                "lg" -> LG
                "xl" -> XL
                else -> UNKNOWN
            }
        }
    }
}

@JsonClass(generateAdapter = false)
enum class TextStyleOffset {
    @Json(name = "sup") SUP,
    @Json(name = "sub") SUB,
    UNKNOWN
}

@Keep
@JsonClass(generateAdapter = true)
data class TextStyle(
    val typeface: String?,
    val color: String?,
    val size: TextStyleSize?,
    val align: TextStyleAlignment?,
    val offset: TextStyleOffset?
)

@Keep
@JsonClass(generateAdapter = true)
data class TitleTextStyle(
    val text: TextStyle?
)

@Keep
@JsonClass(generateAdapter = true)
data class ResourceStyle(
    val title: TitleTextStyle?,
    val subtitle: TitleTextStyle?,
    val description: TitleTextStyle?
)

@Keep
@JsonClass(generateAdapter = true)
data class SegmentStyle(
    val title: TitleTextStyle?,
    val subtitle: TitleTextStyle?,
    val date: TitleTextStyle?
)

@Keep
@JsonClass(generateAdapter = true)
data class Style(
    val resource: ResourceStyle?,
    val segment: SegmentStyle?,
    val blocks: BlocksStyle?,
)

@Keep
@JsonClass(generateAdapter = true)
data class StyleContainer(val style: TitleTextStyle)
