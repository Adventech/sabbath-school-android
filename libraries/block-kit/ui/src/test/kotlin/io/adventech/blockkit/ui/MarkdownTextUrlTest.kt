/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

import io.adventech.blockkit.parser.SpanProcessor
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Link
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MarkdownTextUrlTest(
    private val description: String,
    private val markdown: String,
    private val expectedUrls: List<String>,
) {

    @Test
    fun `preserves link destinations after span processing`() {
        val urls = mutableListOf<String>()
        val parser = Parser.builder().build()
        val visitor = object : AbstractVisitor() {
            override fun visit(text: Text) {
                urls += urlRegex.findAll(text.literal).map { it.value }.toList()
            }

            override fun visit(link: Link) {
                urls += link.destination
                visitChildren(link)
            }
        }

        SpanProcessor().process(markdown).spans.forEach { span ->
            parser.parse(span.text).accept(visitor)
        }

        assertEquals(description, expectedUrls, urls)
    }

    companion object {
        private const val URL = "https://whiteestate.org/absg/3/"
        private const val ZWSP = "\u200B"
        private const val QUERY_URL = "https://example.com/study?week=3&lang=en#further-study"

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf("automatic URL ending in slash", URL, listOf(URL)),
            arrayOf("automatic URL followed by a period", "$URL.", listOf(URL)),
            arrayOf("automatic URL followed by a period and ZWSP", "$URL.$ZWSP", listOf(URL)),
            arrayOf("automatic URL followed by sentence punctuation", "$URL!:,.;$ZWSP", listOf(URL)),
            arrayOf("automatic URL followed by a period and space", "$URL. ", listOf(URL)),
            arrayOf("automatic www URL from issue 1563", "www.whiteestate.org/absg/3.", listOf("www.whiteestate.org/absg/3")),
            arrayOf("ZWSP separates automatic URLs", "$URL$ZWSP$URL", listOf(URL, URL)),
            arrayOf("HTTP URL", "http://example.com/study.$ZWSP", listOf("http://example.com/study")),
            arrayOf("single-character host", "http://a", listOf("http://a")),
            arrayOf("single-character host followed by a period", "http://a.$ZWSP", listOf("http://a")),
            arrayOf("HTTP prefix without a host", "http://", emptyList<String>()),
            arrayOf("HTTPS prefix without a host", "https://", emptyList<String>()),
            arrayOf("www prefix without a host", "www.", emptyList<String>()),
            arrayOf("short URL with query and fragment", "https://a.b?q=1#x.$ZWSP", listOf("https://a.b?q=1#x")),
            arrayOf("empty query and fragment", "https://example.com/?#", listOf("https://example.com/?#")),
            arrayOf("query parameters and fragment", "$QUERY_URL.$ZWSP", listOf(QUERY_URL)),
            arrayOf(
                "encoded punctuation and ZWSP in query",
                "https://example.com/?q=a%2Eb%20c%E2%80%8B#part",
                listOf("https://example.com/?q=a%2Eb%20c%E2%80%8B#part"),
            ),
            arrayOf(
                "punctuation inside query and fragment",
                "https://example.com/?q=a,b;c!d:e.f#part.one.$ZWSP",
                listOf("https://example.com/?q=a,b;c!d:e.f#part.one"),
            ),
            arrayOf("explicit Markdown link", "[Further study]($URL).$ZWSP", listOf(URL)),
            arrayOf("explicit Markdown link with query and fragment", "**[Further study]($QUERY_URL).**", listOf(QUERY_URL)),
            arrayOf("explicit Markdown link with URL label", "[$URL]($URL).", listOf(URL, URL)),
        )
    }
}
