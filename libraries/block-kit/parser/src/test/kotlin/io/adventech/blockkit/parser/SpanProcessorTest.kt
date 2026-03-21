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

package io.adventech.blockkit.parser

import io.adventech.blockkit.parser.span.Span
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

private const val ZWSP = "\u200B" // Zero-Width Space used as boundary guard

class SpanProcessorTest {

    private val processor = SpanProcessor()

    // --- Block-Level Attribute Stripping Tests (Step 1) ---

    @Test
    fun testBlockLevelBoldStripping() {
        val markdown = "**Full string bolded**"
        val block = processor.process(markdown)

        assertEquals(1, block.spans.size)
        assertNotNull(block.attributes)
        assertEquals(true, block.attributes?.bold)

        // Check inner text is clean
        val span = block.spans[0] as Span.Markdown
        assertEquals("${ZWSP}Full string bolded${ZWSP}", span.text)
    }

    @Test
    fun testBlockLevelMixedAttributesStripping() {
        val markdown = "***Full string bolded and italic***"
        val block = processor.process(markdown)

        assertEquals(1, block.spans.size)
        assertNotNull(block.attributes)
        assertEquals(true, block.attributes?.bold)
        assertEquals(true, block.attributes?.italic)

        val span = block.spans[0] as Span.Markdown
        assertEquals("${ZWSP}Full string bolded and italic${ZWSP}", span.text)
    }

    // --- Styled Link Tokenization Tests (Step 2 - Pass 1) ---

    @Test
    fun testStyledLinkWithNestedJsonPayload() {
        // This tests the robust matching of the JSON payload
        val markdown = "Text before ^[styled text]( {\"style\": {\"text\": {\"color\": \"#FFF\"}}} ) text after."
        val block = processor.process(markdown)

        assertEquals(3, block.spans.size)

        // 1. Plain text before
        val span1 = block.spans[0] as Span.Markdown
        assertEquals("${ZWSP}Text before $ZWSP", span1.text)

        // 2. Styled Link
        val span2 = block.spans[1] as Span.StyledMarkdown
        assertEquals("styled text", span2.text)
        assertEquals("{\"style\": {\"text\": {\"color\": \"#FFF\"}}}", span2.json)
        assertNull(span2.attributes)

        // 3. Plain text after
        val span3 = block.spans[2] as Span.Markdown
        assertEquals("$ZWSP text after.${ZWSP}", span3.text)
    }

    // --- Critical 5-Span Test (Standard Link Atomicity and Delimiter Splitting) ---

    @Test
    fun testStyledTextWithComplexRegularLink_shouldReturn5Spans() {
        val linkUrl = "https://example.com/link"
        // The tokens are: TEXT, STYLED, TEXT, STANDARD_LINK, TEXT. This results in 5 Spans.
        val markdown = "Part one. ^[Colored Word]({}) and a [Link with *Italics*]($linkUrl) next."

        val block = processor.process(markdown)

        // CRITICAL ASSERTION: Must be exactly 5 spans.
        assertEquals(5, block.spans.size)

        // 1. Plain text before styled span
        val s1 = block.spans[0] as Span.Markdown
        assertEquals("${ZWSP}Part one. $ZWSP", s1.text)
        assertNull(s1.attributes)

        // 2. Styled Link (Atomic)
        val s2 = block.spans[1] as Span.StyledMarkdown
        assertEquals("Colored Word", s2.text)
        assertEquals("{}", s2.json)
        assertNull(s2.attributes)

        // 3. Plain text between styled link and standard link
        val s3 = block.spans[2] as Span.Markdown
        assertEquals("$ZWSP and a $ZWSP", s3.text)
        assertNull(s3.attributes)

        // 4. The entire Standard Link (Atomic Markdown Block)
        // This proves the standard link was treated as a single atomic token, and no inner delimiters broke it up.
        val expectedLink = "[Link with *Italics*]($linkUrl)"
        val s4 = block.spans[3] as Span.Markdown
        assertEquals("${ZWSP}${expectedLink}${ZWSP}", s4.text)
        assertNull(s4.attributes)

        // 5. Plain text after standard link
        val s5 = block.spans[4] as Span.Markdown
        assertEquals("$ZWSP next.${ZWSP}", s5.text)
        assertNull(s5.attributes)
    }

    @Test
    fun testStandardLinkAtomicityProtection() {
        // Contains standard link with internal delimiter (underscore)
        val linkUrl = "sspmEGW://CSW1011"
        val markdown = "Some words from (Ellen G. White, [_Counsels on Sabbath School Work_, pp. 10, 11]($linkUrl))"
        val block = processor.process(markdown)

        // The input has: Text (before), Standard Link, Text (after)
        // Tokenizer should yield 3 spans: Text, STANDARD_LINK, Text
        assertEquals(3, block.spans.size)

        // 1. Plain text before link
        val span1 = block.spans[0] as Span.Markdown
        val expectedText1 = "Some words from (Ellen G. White, "
        assertEquals("${ZWSP}${expectedText1}${ZWSP}", span1.text)

        // 2. The entire Standard Link (Atomic)
        val span2 = block.spans[1] as Span.Markdown
        val expectedLink = "[_Counsels on Sabbath School Work_, pp. 10, 11]($linkUrl)"
        assertEquals("${ZWSP}${expectedLink}${ZWSP}", span2.text)

        // 3. Plain text after link
        val span3 = block.spans[2] as Span.Markdown
        assertEquals("${ZWSP})${ZWSP}", span3.text)
    }

    // --- Delimiter Resolution Tests (Step 3) ---

    @Test
    fun testDelimiterTogglingAndAttributeApplication() {
        val markdown = "Text **bold text** more text *italic text* final."
        val block = processor.process(markdown)

        // Expected tokens: TEXT, DELIMITER, TEXT, DELIMITER, TEXT, DELIMITER, TEXT, DELIMITER, TEXT
        // This results in 5 final Markdown Spans (TEXT blocks).

        assertEquals(5, block.spans.size)

        // 1. "Text "
        val s1 = block.spans[0] as Span.Markdown
        assertEquals("${ZWSP}Text $ZWSP", s1.text)
        assertNull(s1.attributes) // Bold is OFF

        // 2. "bold text"
        val s2 = block.spans[1] as Span.Markdown
        assertEquals("${ZWSP}bold text${ZWSP}", s2.text)
        assertEquals(true, s2.attributes?.bold) // Bold is ON

        // 3. " more text "
        val s3 = block.spans[2] as Span.Markdown
        assertEquals("$ZWSP more text $ZWSP", s3.text)
        assertNull(s3.attributes) // Bold is OFF

        // 4. "italic text"
        val s4 = block.spans[3] as Span.Markdown
        assertEquals("${ZWSP}italic text${ZWSP}", s4.text)
        assertEquals(true, s4.attributes?.italic) // Italic is ON

        // 5. " final."
        val s5 = block.spans[4] as Span.Markdown
        assertEquals("$ZWSP final.${ZWSP}", s5.text)
        assertNull(s5.attributes) // Italic is OFF
    }

    @Test
    fun testAttributesApplyToStyledLink() {
        // Styled link surrounded by italics delimiters
        val markdown = "This is *^[colored text]({})* in italics."
        val block = processor.process(markdown)

        // Expected tokens: TEXT, DELIMITER, STYLED, DELIMITER, TEXT
        // Expected spans: Markdown, StyledMarkdown, Markdown
        assertEquals(3, block.spans.size)

        // 1. "This is "
        assertNull((block.spans[0] as Span.Markdown).attributes)

        // 2. StyledMarkdown (should inherit the surrounding italic attribute)
        val styledSpan = block.spans[1] as Span.StyledMarkdown
        assertEquals("colored text", styledSpan.text)
        assertNotNull(styledSpan.attributes)
        assertEquals(true, styledSpan.attributes?.italic)

        // 3. " in italics."
        assertNull((block.spans[2] as Span.Markdown).attributes)
    }
}
