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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.content.asText
import app.ss.design.compose.theme.SsTheme
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator

/**
 * Used to display a dialog [Overlay].
 *
 * Usage:
 * ```
 *  val overlayHost = LocalOverlayHost.current
 *  LaunchedEffect(key) {
 *      overlayHost.show(
 *          DialogOverlay<Result>(
 *              title = ContentSpec.Str("Dialog Title"),
 *              cancelButton = DialogOverlay.Button(
 *                  title = ContentSpec.Str("Action 2")
 *              ) { /* Cancel action */ },
 *              confirmButton = DialogOverlay.Button(
 *                  title = ContentSpec.Str("Action 1")
 *              ) { /* Confirm action */ }
 *          ) {
 *              // Dialog content here
 *          }
 *      )
 *  }
 * ```
 */
class DialogOverlay<Result : Any> constructor(
    private val title: ContentSpec,
    private val cancelButton: Button,
    private val confirmButton: Button,
    private val content: @Composable (OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {

    data class Button(
        val title: ContentSpec,
        val action: () -> Unit
    )

    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        Dialog(
            onDismissRequest = cancelButton.action,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(328.dp)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title.asText(),
                        style = SsTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    content(navigator::finish)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = cancelButton.action) { Text(cancelButton.title.asText()) }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(onClick = confirmButton.action) { Text(confirmButton.title.asText()) }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DialogOverlay<Unit>(
                title = ContentSpec.Str("Dialog Title"),
                cancelButton = DialogOverlay.Button(
                    title = ContentSpec.Str("Action 2")
                ) {},
                confirmButton = DialogOverlay.Button(
                    title = ContentSpec.Str("Action 1")
                ) {}
            ) {
                Text(
                    text = "A dialog is a type of modal window that appears in front of app content to provide critical information, " +
                        "or prompt for a decision to be made."
                )
            }.Content { }
        }
    }
}
