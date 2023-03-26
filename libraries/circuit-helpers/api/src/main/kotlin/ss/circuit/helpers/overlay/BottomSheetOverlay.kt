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

package ss.circuit.helpers.overlay

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.launch

/** Used to display a Bottom Sheet [Overlay] **/
class BottomSheetOverlay<Result : Any> constructor(
    private val onDismissRequest: () -> Unit,
    private val skipPartiallyExpanded: Boolean,
    private val content: @Composable ColumnScope.(OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        val bottomSheetState = rememberSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
            initialValue = SheetValue.Hidden
        )
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = bottomSheetState
        ) {
            content(this) { result ->
                scope.launch {
                    bottomSheetState.hide()
                    navigator.finish(result)
                }
            }
        }

        LaunchedEffect(bottomSheetState) { bottomSheetState.show() }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSheetState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    initialValue: SheetValue
): SheetState {
    return rememberSaveable(
        skipPartiallyExpanded, confirmValueChange,
        saver = SheetState.Saver(
            skipPartiallyExpanded = skipPartiallyExpanded,
            confirmValueChange = confirmValueChange
        )
    ) {
        SheetState(skipPartiallyExpanded, initialValue, confirmValueChange)
    }
}
