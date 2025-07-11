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

package ss.share.options

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import io.adventech.blockkit.model.resource.ShareFileURL
import io.adventech.blockkit.model.resource.ShareGroup
import io.adventech.blockkit.model.resource.ShareLinkURL

data class ShareState(
    val segments: List<String>,
    val selectedGroup: ShareGroup,
    val shareButtonState: ShareButtonState,
    val themeColor: Color?,
    val eventSink: (Event) ->  Unit,
): CircuitUiState

enum class ShareButtonState {
    ENABLED,
    DISABLED,
    LOADING
}

sealed interface Event : CircuitUiEvent {
    data class OnSegmentSelected(val segment: String) : Event
    data class OnShareUrlSelected(val url: ShareLinkURL) : Event
    data class OnShareFileClicked(val file: ShareFileURL) : Event
    data class OnShareButtonClicked(val context: Context) : Event
}
