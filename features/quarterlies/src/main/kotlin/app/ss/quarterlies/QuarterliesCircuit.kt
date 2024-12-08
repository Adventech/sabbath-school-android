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

package app.ss.quarterlies

import androidx.compose.runtime.Immutable
import app.ss.models.QuarterlyGroup
import app.ss.quarterlies.model.GroupedQuarterlies
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import ss.services.auth.overlay.AccountDialogOverlay
import ss.services.auth.overlay.UserInfo

@Immutable
data class State(
    val photoUrl: String?,
    val type: GroupedQuarterlies,
    val overlayState: OverlayState?,
    val eventSink: (Event) -> Unit,
) : CircuitUiState

sealed interface Event : CircuitUiEvent {

    /** A quarterly with [index] has been selected.*/
    data class QuarterlySelected(val index: String) : Event

    /** The see all button is clicked on a grouped quarterly [group]. */
    data class SeeAll(val group: QuarterlyGroup) : Event

    /** The profile icon is clicked. */
    data object ProfileClick : Event

    /** The filer languages menu is clicked. */
    data object FilterLanguages : Event
}

sealed interface OverlayState {
    @Immutable
    data class AccountInfo(
        val userInfo: UserInfo,
        val showSettings: Boolean,
        val onResult: (AccountDialogOverlay.Result) -> Unit
    ): OverlayState

    data class BrandingInfo(val onResult: () -> Unit) : OverlayState
}

