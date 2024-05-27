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

package app.ss.lessons

import android.content.Context
import androidx.compose.runtime.Immutable
import app.ss.models.PublishingInfo
import app.ss.models.SSQuarterlyInfo
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonItemSpec
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

sealed interface State : CircuitUiState {
    data object Loading : State
    data object Error : State
    @Immutable
    data class Success(
        val quarterlyInfo: SSQuarterlyInfo,
        val publishingInfo: PublishingInfo?,
        val overlayState: ReadMoreOverlayState?,
        val eventSink: (Event) -> Unit,
    ) : State
}

sealed interface Event : CircuitUiEvent {
    data object OnNavigateBackClick : Event
    data object OnOfflineStateClick : Event
    data class OnShareClick(val context: Context) : Event
    data object OnReadMoreClick : Event
    data class OnLessonClick(val lesson: LessonItemSpec) : Event
    data object OnPublishingInfoClick : Event
}

@Immutable
data class ReadMoreOverlayState(
    val content: String,
    val onResult: (Result) -> Unit
) {
    sealed interface Result : CircuitUiEvent {
        data object Dismissed : Result
    }
}
