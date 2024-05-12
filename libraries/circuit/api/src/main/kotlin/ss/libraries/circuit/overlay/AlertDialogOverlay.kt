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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.content.asText
import app.ss.design.compose.theme.SsTheme
import com.slack.circuitx.overlays.alertDialogOverlay

/**
 * An overlay that shows an [AlertDialog].
 *
 * @see alertDialogOverlay
 */
@OptIn(ExperimentalMaterial3Api::class)
fun ssAlertDialogOverlay(
    title: ContentSpec,
    cancelText: ContentSpec,
    confirmText: ContentSpec,
    usePlatformDefaultWidth: Boolean = false,
    content: @Composable () -> Unit = {},
) =
    alertDialogOverlay(
        confirmButton = {
            TextButton(onClick = it) { Text(confirmText.asText()) }
        },
        dismissButton = {
            TextButton(onClick = it) { Text(cancelText.asText()) }
        },
        title = {
            Text(
                modifier = Modifier,
                text = title.asText(),
                style = SsTheme.typography.headlineSmall,
            )
        },
        text = content,
        properties = DialogProperties(usePlatformDefaultWidth = usePlatformDefaultWidth)
    )
