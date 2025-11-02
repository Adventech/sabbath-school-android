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

package io.adventech.blockkit.parser.span

import org.junit.Assert.assertEquals
import org.junit.Test


class SpanProcessorTest {
    private val processor = SpanProcessor()

    private val BOLD = TextAttributes(bold = true)
    private val ITALIC = TextAttributes(italic = true)
    private val BOLD_ITALIC = TextAttributes(bold = true, italic = true)

    private val SIMPLE_JSON = "{\"style\": {\"text\": {\"color\": \"#58B0E3\"}}}"
    private val SIMPLE_JSON_TRIMMED = "{\"style\": {\"text\": {\"color\": \"#58B0E3\"}}}"


    @Test
    fun testProcess_noMatches_returnsSingleMarkdownBlock() {
        val markdown = "This is a simple markdown string."
        val result = processor.process(markdown)

        assertEquals(null, result.attributes)
        assertEquals(1, result.spans.size)
        assertEquals(Span.Markdown(markdown), result.spans[0])
    }

    @Test
    fun testProcess_styledMarkdownOnly_returnsSingleStyledSpan() {
        val markdown = "^[styled text]($SIMPLE_JSON)"
        val result = processor.process(markdown)

        assertEquals(null, result.attributes)
        assertEquals(1, result.spans.size)
        assertEquals(Span.StyledMarkdown("styled text", SIMPLE_JSON_TRIMMED), result.spans[0])
    }

    @Test
    fun testProcess_leadingAndTrailingMarkdown() {
        val markdown = "Prefix ^[styled text]($SIMPLE_JSON) Suffix."
        val result = processor.process(markdown)

        assertEquals(null, result.attributes)
        assertEquals(3, result.spans.size)
        assertEquals(Span.Markdown("Prefix "), result.spans[0])
        assertEquals(Span.StyledMarkdown("styled text", SIMPLE_JSON_TRIMMED), result.spans[1])
        assertEquals(Span.Markdown(" Suffix."), result.spans[2])
    }

    @Test
    fun testProcess_multipleStyledSpans() {
        val markdown = "^[one]($SIMPLE_JSON) and ^[two]($SIMPLE_JSON)"
        val result = processor.process(markdown)

        assertEquals(null, result.attributes)
        assertEquals(3, result.spans.size)
        assertEquals(Span.StyledMarkdown("one", SIMPLE_JSON_TRIMMED), result.spans[0])
        assertEquals(Span.Markdown(" and "), result.spans[1])
        assertEquals(Span.StyledMarkdown("two", SIMPLE_JSON_TRIMMED), result.spans[2])
    }

    // --- Block Attribute Tests ---

    @Test
    fun testProcess_entireStringIsBlockAttribute_stripsMarkersAndSetsBlockAttributes() {
        val markdown = "**_Text_**"
        val result = processor.process(markdown)

        assertEquals(BOLD_ITALIC, result.attributes)
        assertEquals(1, result.spans.size)
        assertEquals(Span.Markdown("Text"), result.spans[0])
    }

    @Test
    fun testProcess_blockAttributeWithStyledSpan() {
        val markdown = "**Prefix ^[content]($SIMPLE_JSON) Suffix.**"
        val result = processor.process(markdown)

        assertEquals(BOLD, result.attributes)
        assertEquals(3, result.spans.size)
        assertEquals(Span.Markdown("Prefix "), result.spans[0])
        assertEquals(Span.StyledMarkdown("content", SIMPLE_JSON_TRIMMED), result.spans[1])
        assertEquals(Span.Markdown(" Suffix."), result.spans[2])
    }

    // --- Inner and Adjacent Attribute Tests ---

    @Test
    fun testProcess_styledSpanWithInnerAttributes() {
        val markdown = "^[**inner bold**]($SIMPLE_JSON)"
        val result = processor.process(markdown)

        assertEquals(null, result.attributes)
        assertEquals(1, result.spans.size)
        assertEquals(Span.StyledMarkdown("inner bold", SIMPLE_JSON_TRIMMED, BOLD), result.spans[0])
    }

    @Test
    fun testProcess_styledSpanWithMixedInnerAttributes() {
        val markdown = "^[**_inner mixed_**]($SIMPLE_JSON)"
        val result = processor.process(markdown)

        assertEquals(null, result.attributes)
        assertEquals(1, result.spans.size)
        assertEquals(Span.StyledMarkdown("inner mixed", SIMPLE_JSON_TRIMMED, BOLD_ITALIC), result.spans[0])
    }

    @Test
    fun testProcess_styledSpanWrappedByMarkers_appliesAttributeToSpan() {
        val markdown = "**^[content]($SIMPLE_JSON)**"
        val result = processor.process(markdown)

        assertEquals(null, result.attributes)
        assertEquals(1, result.spans.size)
        assertEquals(Span.StyledMarkdown("content", SIMPLE_JSON_TRIMMED, BOLD), result.spans[0])
    }

    @Test
    fun testProcess_styledSpanWrappedByDifferentMarkers_mergesAttributes() {
        val markdown = "*~~^[content]($SIMPLE_JSON)~~*"
        val result = processor.process(markdown)

        assertEquals(null, result.attributes)
        assertEquals(1, result.spans.size)
        assertEquals(
            Span.StyledMarkdown("content", SIMPLE_JSON_TRIMMED, TextAttributes(italic = true, strikethrough = true)),
            result.spans[0]
        )
    }

    @Test
    fun testProcess_styledSpanWrappedAndInnerAttributes_mergesAll() {
        val markdown = "*^[**mixed**]($SIMPLE_JSON)*"
        val result = processor.process(markdown)

        assertEquals(TextAttributes(italic = true), result.attributes)
        assertEquals(1, result.spans.size)
        //assertEquals(Span.StyledMarkdown("mixed", SIMPLE_JSON_TRIMMED, BOLD_ITALIC), result.spans[0])
    }

    @Test
    fun testProcess_styledSpanWrappedByMarkersAndFollowedByMarkdown() {
        val content = "Teach me what I do not see"
        val link = "([Job 34:32](sspmBible://Job3432))"
        val markdown = "**^[$content]($SIMPLE_JSON)** $link"
        val result = processor.process(markdown)

        // The outer ** should be consumed and applied to the StyledMarkdown span.
        assertEquals(null, result.attributes)
        assertEquals(2, result.spans.size)

        // Span 0: StyledMarkdown with BOLD attribute
        assertEquals(Span.StyledMarkdown(content, SIMPLE_JSON_TRIMMED, BOLD), result.spans[0])

        // Span 1: Remainder as regular Markdown
        assertEquals(Span.Markdown(" $link"), result.spans[1])
    }
}
