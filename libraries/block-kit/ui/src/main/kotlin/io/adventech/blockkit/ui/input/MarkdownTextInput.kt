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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import io.adventech.blockkit.ui.TAG_URL
import me.saket.extendedspans.ExtendedSpans
import me.saket.extendedspans.drawBehind

/**
 * Compose Selection API does not currently support exposing the current [androidx.compose.foundation.text.selection.Selection] state when using a
 * [androidx.compose.material3.Text] composable inside the [androidx.compose.foundation.text.selection.SelectionContainer].
 * See https://issuetracker.google.com/issues/142551575 for more information.
 *
 * This is a temporary workaround to allow for the selection of text in a [androidx.compose.foundation.text.BasicTextField] with readOnly set to true.
 */
@Composable
internal fun MarkdownTextInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    extendedSpans: ExtendedSpans,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    textAlign: TextAlign? = null,
    maxLines: Int = Integer.MAX_VALUE,
    onHandleUri: (String) -> Unit = {},
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val styledText = value.annotatedString

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind(extendedSpans),
        readOnly = true,
        textStyle = style.copy(
            textAlign = textAlign ?: style.textAlign,
            platformStyle = PlatformTextStyle(includeFontPadding = true)
        ),
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            extendedSpans.onTextLayout(it)
        },
        interactionSource = remember(styledText) { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->

                        if (interaction is PressInteraction.Release) {
                            val layoutResult = layoutResult.value ?: return@collect
                            val offset = interaction.press.pressPosition
                            val position = layoutResult.getOffsetForPosition(offset)

                            styledText
                                .getStringAnnotations(position, position)
                                .firstOrNull()
                                ?.let { annotation ->
                                    if (annotation.tag == TAG_URL) {
                                        onHandleUri(annotation.item)
                                    }
                                }
                        }
                    }
                }
            }
    )
}
