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

package ss.feed

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import app.ss.models.feed.FeedGroup
import app.ss.models.feed.FeedResource
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import ss.services.auth.overlay.AccountDialogOverlay
import ss.services.auth.overlay.UserInfo

sealed interface State : CircuitUiState {
    val photoUrl: String?
    val overlayState: OverlayState?
    val eventSink: (Event) -> Unit

    data class Loading(
        override val photoUrl: String?,
        override val overlayState: OverlayState?,
        override val eventSink: (Event) -> Unit,
    ) : State

    data class Success(
        override val photoUrl: String?,
        val title: String,
        val groups: ImmutableList<FeedGroup>,
        override val overlayState: OverlayState?,
        override val eventSink: (Event) -> Unit
    ) : State
}

sealed interface Event : CircuitUiEvent {
    /** The profile icon is clicked. */
    data object ProfileClick : Event

    /** The filer languages menu is clicked. */
    data object FilterLanguages : Event
}

sealed interface SuccessEvent : Event {
    /** The see all button for the [group] is clicked. */
    data class OnSeeAllClick(val group: FeedGroup) : Event

    /** A feed [resource] is clicked. */
    data class OnItemClick(val resource: FeedResource) : Event
}

@Stable
sealed interface OverlayState {
    @Immutable
    data class AccountInfo(
        val userInfo: UserInfo,
        val onResult: (AccountDialogOverlay.Result) -> Unit
    ) : OverlayState
}
