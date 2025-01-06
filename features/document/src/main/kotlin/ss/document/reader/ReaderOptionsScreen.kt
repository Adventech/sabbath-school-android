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

package ss.document.reader

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import io.adventech.blockkit.ui.style.ReaderStyle
import kotlinx.parcelize.Parcelize

@Parcelize
data object ReaderOptionsScreen: Screen {
    data class State(
        val theme: ReaderStyle.Theme,
        val typeface: ReaderStyle.Typeface,
        val fontSize: ReaderStyle.Size,
        val eventSink: (Event) -> Unit
    ): CircuitUiState

    sealed interface Event {
        data class OnThemeChanged(val theme: ReaderStyle.Theme): Event
        data class OnTypefaceChanged(val typeface: ReaderStyle.Typeface): Event
        data class OnFontSizeChanged(val size: ReaderStyle.Size): Event
    }
}
