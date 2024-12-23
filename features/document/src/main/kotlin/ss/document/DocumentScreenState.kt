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

package ss.document

import app.ss.models.resource.Segment
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList

sealed interface State : CircuitUiState {
    val title: String
    val eventSink: (Event) -> Unit

    data class Loading(override val title: String, override val eventSink: (Event) -> Unit) : State

    data class Success(
        override val title: String,
        override val eventSink: (Event) -> Unit,
        val initialPage: Int,
        val segments: ImmutableList<Segment>,
    ) : State

}

sealed interface Event : CircuitUiEvent {

    /** Navigation icon is clicked. */
    data object OnNavBack : Event
}

sealed interface SuccessEvent : Event {
    data class OnPageChange(val page: Int) : SuccessEvent
}
