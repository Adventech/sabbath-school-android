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

package io.adventech.blockkit.ui.input

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.core.net.toUri
import io.adventech.blockkit.model.input.Highlight
import io.adventech.blockkit.model.input.Underline
import kotlinx.coroutines.delay

@Composable
internal fun SelectionBlockContainer(
    selection: TextRange?,
    modifier: Modifier = Modifier,
    onHighlight: (Highlight) -> Unit = {},
    onRemoveHighlight: () -> Unit = {},
    onSearchSelection: (TextRange) -> Unit = {},
    onUnderLine: (Underline) -> Unit = {},
    onRemoveUnderline: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var isDestroy by remember { mutableStateOf(false) }
    val canSearchSelection = remember { context.isBrowserInstalled() }

    CompositionLocalProvider(
        LocalTextToolbar provides BlocksTextToolbar(
            view = LocalView.current,
            destroy = isDestroy,
            onHighlight = { color ->
                isDestroy = true

                selection?.takeIf { it.length > 0 }?.let { selection ->
                    val (startIndex, endIndex) = if (selection.end < selection.start) {
                        selection.end to selection.start
                    } else {
                        selection.start to selection.end
                    }

                    onHighlight(
                        Highlight(
                            startIndex = startIndex,
                            endIndex = endIndex,
                            length = selection.length,
                            color = color,
                        )
                    )
                }
            },
            onRemoveHighlight = {
                isDestroy = true

                selection?.let { selection ->
                    if (selection.length > 0) {
                        onRemoveHighlight()
                    }
                }
            },
            onUnderline = { color ->
                isDestroy = true

                selection?.takeIf { it.length > 0 }?.let { selection ->
                    val (startIndex, endIndex) = if (selection.end < selection.start) {
                        selection.end to selection.start
                    } else {
                        selection.start to selection.end
                    }

                    onUnderLine(
                        Underline(
                            startIndex = startIndex,
                            endIndex = endIndex,
                            length = selection.length,
                            color = color,
                        )
                    )
                }
            },
            onRemoveUnderline = {
                isDestroy = true

                selection?.let { selection ->
                    if (selection.length > 0) {
                        onRemoveUnderline()
                    }
                }
            },
            onSearch = if (canSearchSelection) {
                {
                    isDestroy = true
                    selection?.takeIf { it.length > 0 }?.let { selection ->
                        val (startIndex, endIndex) = if (selection.end < selection.start) {
                            selection.end to selection.start
                        } else {
                            selection.start to selection.end
                        }
                        onSearchSelection(TextRange(startIndex, endIndex))
                    }
                }
            } else {
                null
            }
        )
    ) {
        SelectionContainer(
            modifier = modifier,
            content = content,
        )
    }

    LaunchedEffect(selection) {
        if (selection != TextRange.Zero) {
            delay(350)
            isDestroy = false
        }
    }
}

private fun Context.isBrowserInstalled(): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, "https://www.google.com".toUri())
    return intent.resolveActivity(packageManager) != null
}
