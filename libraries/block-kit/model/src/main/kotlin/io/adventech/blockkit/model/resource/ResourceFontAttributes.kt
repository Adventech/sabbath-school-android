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
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = false)
enum class ResourceFontAttributes {
    UNKNOWN,

    @Json(name = "thin") THIN,
    @Json(name = "light") LIGHT,
    @Json(name = "extraLight") EXTRA_LIGHT,
    @Json(name = "regular") REGULAR,
    @Json(name = "medium") MEDIUM,
    @Json(name = "semiBold") SEMI_BOLD,
    @Json(name = "bold") BOLD,
    @Json(name = "extraBold") EXTRA_BOLD,
    @Json(name = "black") BLACK,

    @Json(name = "thin-italic") THIN_ITALIC,
    @Json(name = "light-italic") LIGHT_ITALIC,
    @Json(name = "extraLight-italic") EXTRA_LIGHT_ITALIC,
    @Json(name = "regular-italic") REGULAR_ITALIC,
    @Json(name = "medium-italic") MEDIUM_ITALIC,
    @Json(name = "semiBold-italic") SEMI_BOLD_ITALIC,
    @Json(name = "bold-italic") BOLD_ITALIC,
    @Json(name = "extraBold-italic") EXTRA_BOLD_ITALIC,
    @Json(name = "black-italic") BLACK_ITALIC
}
