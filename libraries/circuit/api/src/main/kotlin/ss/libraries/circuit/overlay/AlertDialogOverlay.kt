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

package ss.libraries.circuit.overlay

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.content.asText
import app.ss.design.compose.theme.SsTheme
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuitx.overlays.DialogResult
import com.slack.circuitx.overlays.DialogResult.Cancel
import com.slack.circuitx.overlays.DialogResult.Confirm
import com.slack.circuitx.overlays.DialogResult.Dismiss

/**
 * An overlay that shows an [AlertDialog].
 *
 */
fun ssAlertDialogOverlay(
    title: ContentSpec,
    cancelText: ContentSpec,
    confirmText: ContentSpec,
    usePlatformDefaultWidth: Boolean = true,
    content: @Composable () -> Unit = {},
): AlertDialogOverlay<*, DialogResult> {
    return AlertDialogOverlay(
        model = Unit,
        onDismissRequest = { Dismiss },
    ) { _, navigator ->
        AlertDialog(
            onDismissRequest = { navigator.finish(Dismiss) },
            icon = null,
            title = {
                Text(
                    modifier = Modifier,
                    text = title.asText(),
                    style = SsTheme.typography.headlineSmall,
                )
            },
            text = content,
            confirmButton = {
                TextButton(onClick = { navigator.finish(Confirm) }) { Text(confirmText.asText()) }
            },
            dismissButton = {
                TextButton(onClick = { navigator.finish(Cancel)}) { Text(cancelText.asText()) }
            },
            properties = DialogProperties(usePlatformDefaultWidth = usePlatformDefaultWidth)
        )
    }
}

class AlertDialogOverlay <Model : Any, Result : Any>(
    private val model: Model,
    private val onDismissRequest: () -> Result,
    private val properties: DialogProperties = DialogProperties(),
    private val content: @Composable (Model, OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        BasicAlertDialog(
            onDismissRequest = {
                navigator.finish(onDismissRequest())
            },
            modifier = Modifier,
            properties = properties,
            content = {
                Surface(
                    shape = AlertDialogDefaults.shape,
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                ) {
                    content(model, navigator::finish)
                }
            },
        )
    }

}
