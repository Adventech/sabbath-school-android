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

package ss.document.segment.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.SsTheme
import io.adventech.blockkit.model.SegmentStyle
import io.adventech.blockkit.ui.MarkdownText
import io.adventech.blockkit.ui.style.Styler
import ss.misc.DateHelper

@Composable
internal fun SegmentHeader(
    title: String,
    subtitle: String?,
    date: String?,
    contentColor: Color,
    style: SegmentStyle?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        subtitle?.let {
            SelectionContainer {
                MarkdownText(
                    markdownText = it.uppercase(),
                    modifier = Modifier.fillMaxWidth(),
                    style = (style?.subtitle?.text?.let {
                        Styler.textStyle(it)
                    } ?: SsTheme.typography.titleLarge).copy(
                        fontSize = 15.sp
                    ),
                    color = contentColor.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = style?.let { Styler.textAlign(it.subtitle?.text) }
                )
            }
        }

        date?.dateDisplay()?.let {
            SelectionContainer {
                Text(
                    text = it.uppercase(),
                    modifier = Modifier.fillMaxWidth(),
                    style = (style?.date?.text?.let {
                        Styler.textStyle(it)
                    } ?: SsTheme.typography.titleLarge).copy(
                        fontSize = 15.sp
                    ),
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        SelectionContainer {
            MarkdownText(
                markdownText = title,
                modifier = Modifier.fillMaxWidth(),
                style = (style?.title?.text?.let {
                    Styler.textStyle(it).copy(
                        fontSize = 30.sp,
                        lineHeight = 30.sp
                    )
                } ?: SsTheme.typography.titleLarge).copy(
                    fontSize = 30.sp,
                    lineHeight = 30.sp
                ),
                color = style?.title?.text?.color?.let { Styler.textColor(style.title?.text) } ?: contentColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = style?.let { Styler.textAlign(it.title?.text) }
            )
        }
    }
}

internal fun String?.dateDisplay(): String? {
    return this?.let { DateHelper.formatDate(it) }
}
