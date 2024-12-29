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

package io.adventech.blockkit.parser

import io.adventech.blockkit.model.AttributedText
import io.adventech.blockkit.model.TextStyle
import io.adventech.blockkit.model.TextStyleSize
import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class AttributedTextParserTest {

    private val parser = AttributedTextParser()

    @Test
    fun `parse - custom text style attributes`() {
        val markdown = "^[Part 2]({\"style\": {\"text\": {\"color\": \"#a65726\", \"size\": \"xl\", \"typeface\": \"BaskervilleBT-Bold\"}}})â€”The Sabbath School Program"

        val attributes = parser.parse(markdown)
        assertEquals(2, attributes.size)
        assertEquals("Part 2", (attributes[0] as AttributedText.Styled).label)
        assertEquals(
            TextStyle(
                typeface = "BaskervilleBT-Bold",
                color = "#a65726",
                size = TextStyleSize.XL,
                align = null,
                offset = null,
            ), (attributes[0] as AttributedText.Styled).style
        )
    }

    @Test
    fun `parse - multiple styled attributes in the same markdown`() {
        val markdown = "^[Part 1]({\"style\": {\"text\": {\"color\": \"#a65726\", \"size\": \"xl\", \"typeface\": \"BaskervilleBT-Bold\"}}})â€”The Sabbath School Program ^[Part 2]({\"style\": {\"text\": {\"color\": \"#a65726\", \"size\": \"xl\", \"typeface\": \"BaskervilleBT-Bold\"}}})â€”The Sabbath School Program"

        val attributes = parser.parse(markdown)
        assertEquals(4, attributes.size)
        assertEquals("Part 1", (attributes[0] as AttributedText.Styled).label)
        assertEquals(
            TextStyle(
                typeface = "BaskervilleBT-Bold",
                color = "#a65726",
                size = TextStyleSize.XL,
                align = null,
                offset = null,
            ), (attributes[0] as AttributedText.Styled).style
        )
        assertEquals("â€”The Sabbath School Program", (attributes[1] as AttributedText.Plain).label)
        assertEquals("Part 2", (attributes[2] as AttributedText.Styled).label)
        assertEquals(
            TextStyle(
                typeface = "BaskervilleBT-Bold",
                color = "#a65726",
                size = TextStyleSize.XL,
                align = null,
                offset = null,
            ), (attributes[2] as AttributedText.Styled).style
        )
        assertEquals("â€”The Sabbath School Program", (attributes[3] as AttributedText.Plain).label)
    }

    @Test
    fun `parse - no custom styled attributes`() {
        val markdown = "Part 1 â€” The Sabbath School Program"

        val attributes = parser.parse(markdown)
        assertEquals(1, attributes.size)
        assertEquals("Part 1 â€” The Sabbath School Program", (attributes[0] as AttributedText.Plain).label)
    }
}
