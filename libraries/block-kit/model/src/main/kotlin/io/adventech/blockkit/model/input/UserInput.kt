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

package io.adventech.blockkit.model.input

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.adapters.AdaptedBy
import io.adventech.blockkit.model.adapter.UserInputJsonAdapterFactory
import java.util.UUID

@Keep
@AdaptedBy(UserInputJsonAdapterFactory::class)
sealed interface UserInput {
    val blockId: String

    @JsonClass(generateAdapter = true)
    data class Appeal(
        override val blockId: String,
        val appeal: Boolean
    ) : UserInput

    @JsonClass(generateAdapter = true)
    data class Checklist(
        override val blockId: String,
        val checked: List<Int>
    ) : UserInput

    @JsonClass(generateAdapter = true)
    data class Comment(
        override val blockId: String,
        val comment: String
    ) : UserInput

    @JsonClass(generateAdapter = true)
    data class Completion(
        override val blockId: String,
        val completion: Map<String, String>
    ) : UserInput

    @JsonClass(generateAdapter = true)
    data class Highlights(
        override val blockId: String,
        val highlights: List<Highlight>
    ) : UserInput

    @JsonClass(generateAdapter = true)
    data class MultipleChoice(
        override val blockId: String,
        val choice: Int
    ) : UserInput

    @JsonClass(generateAdapter = true)
    data class Poll(
        override val blockId: String,
        val vote: Int
    ) : UserInput

    @JsonClass(generateAdapter = true)
    data class Question(
        override val blockId: String,
        val answer: String
    ) : UserInput

    @JsonClass(generateAdapter = true)
    data class Annotation(
        override val blockId: String,
        val pdfId: String,
        val data: List<PDFAuxAnnotations>,
    ) : UserInput

    data object Unknown : UserInput {
        override val blockId: String = UUID.randomUUID().toString()
    }
}
