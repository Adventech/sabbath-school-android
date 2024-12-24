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
import io.adventech.blockkit.model.input.UserInput
import java.lang.reflect.Type

@Keep
class UserInputJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: Set<Annotation?>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        return PolymorphicJsonAdapterFactory.of(UserInput::class.java, "inputType")
            .withSubtype(UserInput.Appeal::class.java, "appeal")
            .withSubtype(UserInput.Checklist::class.java, "checklist")
            .withSubtype(UserInput.Comment::class.java, "comment")
            .withSubtype(UserInput.Completion::class.java, "completion")
            .withSubtype(UserInput.Highlights::class.java, "highlights")
            .withSubtype(UserInput.MultipleChoice::class.java, "multiple-choice")
            .withSubtype(UserInput.Poll::class.java, "poll")
            .withSubtype(UserInput.Question::class.java, "question")
            .withSubtype(UserInput.Annotation::class.java, "annotation")
            .withDefaultValue(UserInput.Unknown)
            .create(type, annotations, moshi)
    }
}
