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

package app.ss.design.compose.extensions.snackbar

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.ss.design.compose.extensions.content.ContentSpec

/** The [SnackbarHostState] shown in the Notification settings screens. */
data class SsSnackbarState(
    val message: ContentSpec,
    val actionLabel: ContentSpec? = null,
    val withDismissAction: Boolean = true,
    val duration: SnackbarDuration = SnackbarDuration.Long,
    val onResult: (SnackbarResult) -> Unit,
)


/** The [SnackbarHostState] shown in the [androidx.compose.material3.Scaffold]. */
@Composable
fun rememberSsSnackbarState(
    state: SsSnackbarState?
): SnackbarHostState {
    val snackbarHostState = remember { SnackbarHostState() }
    if (state != null) {
        val context = LocalContext.current
        LaunchedEffect(state) {
            state.onResult(
                snackbarHostState.showSnackbar(
                    object : SnackbarVisuals {
                        override val actionLabel: String? = state.actionLabel?.asString(context)
                        override val duration: SnackbarDuration = state.duration
                        override val message: String = state.message.asString(context)
                        override val withDismissAction: Boolean = state.withDismissAction
                    }
                )
            )
        }
    } else {
        snackbarHostState.currentSnackbarData?.dismiss()
    }
    return snackbarHostState
}

private fun ContentSpec.asString(context: Context): String {
    return when (this) {
        is ContentSpec.Str -> content
        is ContentSpec.Res -> context.getString(content)
        else -> ""
    }
}

