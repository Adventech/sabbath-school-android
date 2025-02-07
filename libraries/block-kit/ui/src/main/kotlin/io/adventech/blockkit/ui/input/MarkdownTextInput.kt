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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.adventech.blockkit.model.input.Highlight
import io.adventech.blockkit.ui.TAG_URL
import io.adventech.blockkit.ui.rememberMarkdownText
import io.adventech.blockkit.ui.style.BlockStyleTemplate
import io.adventech.blockkit.ui.style.StyleTemplate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.saket.extendedspans.ExtendedSpans
import me.saket.extendedspans.RoundedCornerSpanPainter
import me.saket.extendedspans.RoundedCornerSpanPainter.TextPaddingValues
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
    markdownText: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    textAlign: TextAlign? = null,
    styleTemplate: StyleTemplate = BlockStyleTemplate.DEFAULT,
    maxLines: Int = Integer.MAX_VALUE,
    highlights: ImmutableList<Highlight> = persistentListOf(),
    onHandleUri: (String) -> Unit = {},
) {
    val styledText = rememberMarkdownText(markdownText, style, styleTemplate, color, highlights)

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val extendedSpans = remember {
        ExtendedSpans(
            RoundedCornerSpanPainter(
                cornerRadius = 6.sp,
                padding = TextPaddingValues(horizontal = 4.sp),
                topMargin = 2.sp,
                bottomMargin = 2.sp,
                stroke = RoundedCornerSpanPainter.Stroke(
                    color = Color.Transparent,
                ),
            )
        )
    }

    val text = remember(styledText) {
        extendedSpans.extend(styledText)
    }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text)) }

    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onValueChange(it)
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    layoutResult.value?.let { layoutResult ->
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
            .drawBehind(extendedSpans),
        readOnly = true,
        textStyle = style.copy(
            textAlign = textAlign ?: style.textAlign,
        ),
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            extendedSpans.onTextLayout(it)
        },
    )
}
