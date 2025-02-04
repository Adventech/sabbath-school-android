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
import io.adventech.blockkit.model.input.UserInputRequest
import io.adventech.blockkit.model.input.UserInputType
import java.lang.reflect.Type

@Keep
class UserInputJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: Set<Annotation?>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        return PolymorphicJsonAdapterFactory.of(UserInput::class.java, "inputType")
            .withSubtype(UserInput.Appeal::class.java, UserInputType.APPEAL)
            .withSubtype(UserInput.Checklist::class.java, UserInputType.CHECKLIST)
            .withSubtype(UserInput.Comment::class.java, UserInputType.COMMENT)
            .withSubtype(UserInput.Completion::class.java, UserInputType.COMPLETION)
            .withSubtype(UserInput.Highlights::class.java, UserInputType.HIGHLIGHTS)
            .withSubtype(UserInput.MultipleChoice::class.java, UserInputType.MULTIPLE_CHOICE)
            .withSubtype(UserInput.Poll::class.java, UserInputType.POLL)
            .withSubtype(UserInput.Question::class.java, UserInputType.QUESTION)
            .withSubtype(UserInput.Annotation::class.java, UserInputType.ANNOTATION)
            .withDefaultValue(UserInput.Unknown)
            .create(type, annotations, moshi)
    }
}

@Keep
class UserInputRequestJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: Set<Annotation?>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        return PolymorphicJsonAdapterFactory.of(UserInputRequest::class.java, "inputType")
            .withSubtype(UserInputRequest.Appeal::class.java, UserInputType.APPEAL)
            .withSubtype(UserInputRequest.Checklist::class.java, UserInputType.CHECKLIST)
            .withSubtype(UserInputRequest.Comment::class.java, UserInputType.COMMENT)
            .withSubtype(UserInputRequest.Completion::class.java, UserInputType.COMPLETION)
            .withSubtype(UserInputRequest.Highlights::class.java, UserInputType.HIGHLIGHTS)
            .withSubtype(UserInputRequest.MultipleChoice::class.java, UserInputType.MULTIPLE_CHOICE)
            .withSubtype(UserInputRequest.Poll::class.java, UserInputType.POLL)
            .withSubtype(UserInputRequest.Question::class.java, UserInputType.QUESTION)
            .withSubtype(UserInputRequest.Annotation::class.java, UserInputType.ANNOTATION)
            .withDefaultValue(UserInputRequest.Unknown)
            .create(type, annotations, moshi)
    }
}
