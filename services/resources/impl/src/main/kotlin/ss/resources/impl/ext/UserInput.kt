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

package ss.resources.impl.ext

import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import io.adventech.blockkit.model.input.UserInputType

/** Generates and returns a unique id for the [UserInput]. **/
fun UserInput.localId(documentId: String): String {
    return "$documentId-${type()}-$blockId"
}

fun UserInputRequest.localId(documentId: String): String {
    return "$documentId-${type()}-$blockId"
}

fun UserInput.type(): String {
    return when (this) {
        is UserInput.Annotation -> UserInputType.ANNOTATION
        is UserInput.Appeal -> UserInputType.APPEAL
        is UserInput.Checklist -> UserInputType.CHECKLIST
        is UserInput.Comment -> UserInputType.COMMENT
        is UserInput.Completion -> UserInputType.COMPLETION
        is UserInput.Highlights -> UserInputType.HIGHLIGHTS
        is UserInput.MultipleChoice -> UserInputType.MULTIPLE_CHOICE
        is UserInput.Poll -> UserInputType.POLL
        is UserInput.Question -> UserInputType.QUESTION
        UserInput.Unknown -> UserInputType.UNKNOWN
    }
}

fun UserInputRequest.type(): String {
    return when (this) {
        is UserInputRequest.Annotation -> UserInputType.ANNOTATION
        is UserInputRequest.Appeal -> UserInputType.APPEAL
        is UserInputRequest.Checklist -> UserInputType.CHECKLIST
        is UserInputRequest.Comment -> UserInputType.COMMENT
        is UserInputRequest.Completion -> UserInputType.COMPLETION
        is UserInputRequest.Highlights -> UserInputType.HIGHLIGHTS
        is UserInputRequest.MultipleChoice -> UserInputType.MULTIPLE_CHOICE
        is UserInputRequest.Poll -> UserInputType.POLL
        is UserInputRequest.Question -> UserInputType.QUESTION
        UserInputRequest.Unknown -> UserInputType.UNKNOWN
    }
}

fun UserInputRequest.toInput(id: String, timestamp: Long): UserInput {
    return when (this) {
        is UserInputRequest.Annotation -> UserInput.Annotation(blockId, id, timestamp, pdfId, data)
        is UserInputRequest.Appeal -> UserInput.Appeal(blockId, id, timestamp, appeal)
        is UserInputRequest.Checklist -> UserInput.Checklist(blockId, id, timestamp, checked)
        is UserInputRequest.Comment -> UserInput.Comment(blockId, id, timestamp, comment)
        is UserInputRequest.Completion -> UserInput.Completion(blockId, id, timestamp, completion)
        is UserInputRequest.Highlights -> UserInput.Highlights(blockId, id, timestamp, highlights)
        is UserInputRequest.MultipleChoice -> UserInput.MultipleChoice(blockId, id, timestamp, choice)
        is UserInputRequest.Poll -> UserInput.Poll(blockId, id, timestamp, vote)
        is UserInputRequest.Question -> UserInput.Question(blockId, id, timestamp, answer)
        UserInputRequest.Unknown -> throw IllegalArgumentException("Unknown UserInput type")
    }
}
