/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

package ss.document.segment.components.pdf

import android.content.Context
import androidx.compose.runtime.Immutable
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.configuration.theming.ThemeMode
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import io.adventech.blockkit.model.input.PDFAuxAnnotations
import kotlinx.collections.immutable.ImmutableList
import ss.document.components.DocumentTopAppBarAction
import ss.libraries.circuit.overlay.BottomSheetOverlay
import ss.libraries.pdf.api.LocalFile

sealed interface ReadPdfState : CircuitUiState {
    data object Loading : ReadPdfState

    data class Success(
        val documents: ImmutableList<PdfDocumentState>,
        val config: PdfReaderConfig,
        val topAppBarState: PdfTopAppBarState,
        val overlayState: ReadPdfOverlayState,
        val eventSink: (ReadPdfEvent) -> Unit,
    ) : ReadPdfState
}

sealed interface ReadPdfEvent {
    data object OnNavBack : ReadPdfEvent
    data class OnNavEvent(val event: NavEvent, val context: Context) : ReadPdfEvent
    data class OnTopAppBarAction(val action: DocumentTopAppBarAction): ReadPdfEvent
    data class OnDocumentHidden(
        val config: PdfConfiguration,
        val annotations: List<Annotation>?,
        val pdfId: String,
    ) : ReadPdfEvent
}

sealed interface ReadPdfOverlayState : CircuitUiState {

    data object None : ReadPdfOverlayState

    /** Overlay state for a bottom sheet. */
    @Immutable
    data class BottomSheet(
        val screen: Screen,
        val skipPartiallyExpanded: Boolean,
        val onResult: (BottomSheetOverlay.Result) -> Unit,
    ) : ReadPdfOverlayState
}

@Immutable
data class PdfDocumentState(
    val pdfId: String,
    val file: LocalFile,
    val annotations: ImmutableList<PDFAuxAnnotations>,
)

@Immutable
data class PdfReaderConfig(
    val scrollMode: PageScrollMode,
    val layoutMode: PageLayoutMode,
    val scrollDirection: PageScrollDirection,
    val themeMode: ThemeMode,
)

