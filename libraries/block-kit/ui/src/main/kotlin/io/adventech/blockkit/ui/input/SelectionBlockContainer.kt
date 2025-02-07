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

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.adventech.blockkit.model.input.Highlight
import kotlinx.coroutines.delay

@Composable
internal fun SelectionBlockContainer(
    textFieldValue: TextFieldValue?,
    modifier: Modifier = Modifier,
    onHighlight: (Highlight) -> Unit = {},
    content: @Composable () -> Unit,
) {
    var isDestroy = remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalTextToolbar provides BlocksTextToolbar(
            view = LocalView.current,
            destroy = isDestroy.value,
            onHighlight = { color ->
                isDestroy.value = true

                textFieldValue?.selection?.let { selection ->
                    onHighlight(
                        Highlight(
                            startIndex = selection.start,
                            endIndex = selection.end,
                            length = selection.length,
                            color = color,
                        )
                    )
                }
            },
        )
    ) {
        SelectionContainer(
            modifier = modifier,
            content = content,
        )
    }

    LaunchedEffect(textFieldValue?.selection) {
        if (textFieldValue?.selection != TextRange.Zero) {
            delay(350)
            isDestroy.value = false
        }
    }
}
