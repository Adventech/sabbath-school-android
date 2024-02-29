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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.DialogProperties
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.content.asText
import app.ss.design.compose.theme.SsTheme
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator

/**
 * Used to display an [AlertDialog] circuit [Overlay].
 *
 * Usage:
 * ``` kotlin
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
class DialogOverlay<Result : Any>(
    private val title: ContentSpec,
    private val cancelButton: Button,
    private val confirmButton: Button,
    private val content: @Composable (OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {

  data class Button(val title: ContentSpec, val action: () -> Unit)

  @Composable
  override fun Content(navigator: OverlayNavigator<Result>) {
    AlertDialog(
        onDismissRequest = cancelButton.action,
        confirmButton = {
          TextButton(onClick = confirmButton.action) { Text(confirmButton.title.asText()) }
        },
        modifier = Modifier,
        dismissButton = {
          TextButton(onClick = cancelButton.action) { Text(cancelButton.title.asText()) }
        },
        title = {
          Text(
              modifier = Modifier.fillMaxWidth(),
              text = title.asText(),
              style = SsTheme.typography.headlineSmall,
          )
        },
        text = { content(navigator::finish) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    )
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
