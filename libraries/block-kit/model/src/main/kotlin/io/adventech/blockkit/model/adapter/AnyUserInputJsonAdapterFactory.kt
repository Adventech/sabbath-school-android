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
import io.adventech.blockkit.model.input.AnyUserInput
import java.lang.reflect.Type

@Keep
class AnyUserInputJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: Set<Annotation?>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        return PolymorphicJsonAdapterFactory.of(AnyUserInput::class.java, "inputType")
            .withSubtype(AnyUserInput.Appeal::class.java, "appeal")
            .withSubtype(AnyUserInput.Checklist::class.java, "checklist")
            .withSubtype(AnyUserInput.Comment::class.java, "comment")
            .withSubtype(AnyUserInput.Completion::class.java, "completion")
            .withSubtype(AnyUserInput.Highlights::class.java, "highlights")
            .withSubtype(AnyUserInput.MultipleChoice::class.java, "multiple-choice")
            .withSubtype(AnyUserInput.Poll::class.java, "poll")
            .withSubtype(AnyUserInput.Question::class.java, "question")
            .withSubtype(AnyUserInput.Annotation::class.java, "annotation")
            .withDefaultValue(AnyUserInput.Unknown)
            .create(type, annotations, moshi)
    }
}
