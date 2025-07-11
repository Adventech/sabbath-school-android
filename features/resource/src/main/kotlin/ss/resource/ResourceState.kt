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

package ss.resource

import android.content.Context
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ShareOptions
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.ImmutableList
import ss.libraries.circuit.overlay.BottomSheetOverlay
import ss.resource.components.content.ResourceSectionSpec
import ss.resource.components.spec.CreditSpec
import ss.resource.components.spec.FeatureSpec
import ss.resource.components.spec.SharePosition

sealed interface State: CircuitUiState {

    val title: String
    val eventSink: (Event) -> Unit

    data class Loading(
        override val title: String,
        override val eventSink: (Event) -> Unit
    ): State

    data class Success(
        override val title: String,
        val readDocumentTitle: String?,
        val resource: Resource,
        val sections: ImmutableList<ResourceSectionSpec>,
        val credits: ImmutableList<CreditSpec>,
        val features: ImmutableList<FeatureSpec>,
        val fontFamilyProvider: FontFamilyProvider,
        val overlayState: ResourceOverlayState?,
        val sharePosition: SharePosition,
        val primaryColorDark: String,
        override val eventSink: (Event) -> Unit
    ): State

}

sealed interface Event : CircuitUiEvent {
    /** Navigation icon is clicked. */
    data object OnNavBack : Event

    /** CTA button is clicked. */
    data object OnCtaClick : Event

    /** Read more button is clicked. */
    data object OnReadMoreClick : Event

    /** Share button is clicked. */
    data class OnShareClick(val context: Context) : Event
}

sealed interface ResourceOverlayState : CircuitUiState {

    data class IntroductionBottomSheet(
        val markdown: String,
        val onResult: (BottomSheetOverlay.Result) -> Unit
    ): ResourceOverlayState

    data class ShareBottomSheet(
        val options: ShareOptions,
        val primaryColorDark: String,
        val title: String,
        val onResult: (BottomSheetOverlay.Result) -> Unit,
    ): ResourceOverlayState
}
