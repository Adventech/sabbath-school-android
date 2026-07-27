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

package io.adventech.blockkit.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import me.saket.extendedspans.ExtendedSpans
import me.saket.extendedspans.RoundedCornerSpanPainter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class MarkdownLinkClickTest {

    @Test
    fun `highlight decoration before URL does not block link click`() {
        val uri = "ss-bible://John.3.16"
        val source = buildAnnotatedString {
            append("John 3:16")
            addStyle(SpanStyle(background = Color.Yellow), 0, length)
            addStringAnnotation(TAG_URL, uri, 0, length)
        }
        val text = ExtendedSpans(
            RoundedCornerSpanPainter(
                cornerRadius = 6.sp,
                padding = RoundedCornerSpanPainter.TextPaddingValues(horizontal = 4.sp),
                topMargin = 2.sp,
                bottomMargin = 2.sp,
                stroke = RoundedCornerSpanPainter.Stroke(color = Color.Transparent),
            )
        ).extend(source)

        text.getStringAnnotations(4, 4).first().tag shouldBeEqualTo "rounded_corner_span"
        text.urlAt(4) shouldBeEqualTo uri
    }
}
