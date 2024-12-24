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
data class BlockStyle(
    val block: BlockLevelStyle?,
    val wrapper: BlockLevelStyle?,
    val image: BlockImageStyle?,
    val text: TextStyle?,
)

@JsonClass(generateAdapter = false)
data class BlockLevelStyle(
    val backgroundColor: String?,
    val backgroundImage: String?,
    val backgroundPosition: BackgroundPositionStyle?,
    val padding: SpacingStyle?,
    val margin: SpacingStyle?,
    val rounded: Boolean?
)

@JsonClass(generateAdapter = false)
data class BlockStyleOverride(
    val type: BlockType,
    val style: BlockStyle
)

@JsonClass(generateAdapter = false)
data class BlockImageStyle(
    val aspectRatio: Float?,
    val expandable: Boolean?
)

@JsonClass(generateAdapter = false)
enum class BlockStyleSpacing {
    UNKNOWN,
    @Json(name = "none")
    NONE,
    @Json(name = "xs")
    XS,
    @Json(name = "sm")
    SM,
    @Json(name = "base")
    BASE,
    @Json(name = "lg")
    LG,
    @Json(name = "xl")
    XL
}

@JsonClass(generateAdapter = false)
data class BackgroundPositionStyle(
    val x: BlockStylePositionX,
    val y: BlockStylePositionY
)

@JsonClass(generateAdapter = false)
data class SpacingStyle(
    val top: BlockStyleSpacing?,
    val bottom: BlockStyleSpacing?,
    val start: BlockStyleSpacing?,
    val end: BlockStyleSpacing?
)

@JsonClass(generateAdapter = false)
enum class BlockStylePositionX {
    UNKNOWN,
    @Json(name = "start")
    START,
    @Json(name = "end")
    END,
    @Json(name = "center")
    CENTER
}

@JsonClass(generateAdapter = false)
enum class BlockStylePositionY {
    UNKNOWN,
    @Json(name = "top")
    TOP,
    @Json(name = "bottom")
    BOTTOM,
    @Json(name = "center")
    CENTER
}

@JsonClass(generateAdapter = false)
enum class ImageStyleTextAlignment {
    UNKNOWN,
    @Json(name = "top")
    TOP,
    @Json(name = "bottom")
    BOTTOM
}

@JsonClass(generateAdapter = false)
data class BlocksStyle(
    val inline: InlineBlocksStyle?,
    val nested: NestedBlocksStyle?
)

@JsonClass(generateAdapter = false)
data class InlineBlocksStyle(
    val all: BlockStyle?,
    val blocks: List<BlockStyleOverride>?
)

@JsonClass(generateAdapter = false)
data class NestedBlocksStyle(
    val all: BlockStyle?,
    val blocks: List<BlockStyleOverride>?
)
