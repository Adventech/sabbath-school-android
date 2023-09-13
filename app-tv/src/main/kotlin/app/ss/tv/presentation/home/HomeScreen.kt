/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.tv.presentation.home

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import app.ss.tv.data.model.CategorySpec
import app.ss.tv.data.model.VideoSpec
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize

@Parcelize
object HomeScreen : Screen, Parcelable {

    sealed interface Event : CircuitUiEvent {
        data object OnBack : Event
        data class OnVideoClick(val video: VideoSpec) : Event
    }

    sealed interface State : CircuitUiState {

        val eventSink: (Event) -> Unit

        data class Loading(override val eventSink: (Event) -> Unit) : State

        data class Error(override val eventSink: (Event) -> Unit) : State

        @Immutable
        data class Videos(
            val categories: ImmutableList<CategorySpec>,
            override val eventSink: (Event) -> Unit
        ) : State
    }
}
