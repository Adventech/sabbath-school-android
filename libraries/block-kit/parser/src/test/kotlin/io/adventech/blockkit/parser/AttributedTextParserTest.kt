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

import io.adventech.blockkit.model.TextStyle
import io.adventech.blockkit.model.TextStyleSize
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.commonmark.node.Text
import org.junit.Test

class AttributedTextParserTest {

    private val parser = AttributedTextParser()

    @Test
    fun `replaceNode - detect an InlineAttributeNode`() {
        val markdown = "^[Part 2]({\"style\": {\"text\": {\"color\": \"#a65726\", \"size\": \"xl\", \"typeface\": \"BaskervilleBT-Bold\"}}})â€”The Sabbath School Program"

        val node = parser.replaceNode(Text(markdown))

        node shouldBeInstanceOf InlineAttributeNode::class
        node as InlineAttributeNode

        node.style shouldBeEqualTo TextStyle(
            typeface = "BaskervilleBT-Bold",
            color = "#a65726",
            size = TextStyleSize.XL,
            align = null,
            offset = null,
        )
    }

    @Test
    fun `replaceNode - no custom styled attributes - node is not replaced`() {
        val markdown = "Part 1 â€” The Sabbath School Program"
        val original = Text(markdown)

        val replaced = parser.replaceNode(original)
        replaced shouldBeEqualTo original
    }

    @Test
    fun `parseTypeface - returns all custom typefaces in the inline styles`() {
        val markdown = "^[Part 1]({\"style\": {\"text\": {\"color\": \"#a65726\", \"size\": \"xl\", \"typeface\": \"BaskervilleBT-Bold\"}}})â€”The Sabbath School Program ^[Part 2]({\"style\": {\"text\": {\"color\": \"#a65726\", \"size\": \"xl\", \"typeface\": \"BaskervilleBT-Bold\"}}})â€”The Sabbath School Program"

        val typefaces = parser.parseTypeface(markdown)

        typefaces shouldBeEqualTo setOf("BaskervilleBT-Bold")
    }
}
