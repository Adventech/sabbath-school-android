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

package app.ss.auth

import android.content.Context
import app.ss.design.compose.extensions.snackbar.SsSnackbarState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

sealed interface State : CircuitUiState {
    data class Default(
        val snackbarState: SsSnackbarState?,
        val eventSink: (Event) -> Unit
    ) : State
    data object Loading : State
    data class ConfirmSignInAnonymously(val eventSink: (OverlayEvent) -> Unit) : State
}

sealed interface Event : CircuitUiEvent {
    data class SignInWithGoogle(val context: Context) : Event
    data object SignInAnonymously : Event
    data object OpenPrivacyPolicy : Event
}

sealed interface OverlayEvent {
    data object Dismiss : OverlayEvent
    data object Confirm : OverlayEvent
}
