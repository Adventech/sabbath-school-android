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

import androidx.compose.runtime.Stable
import io.adventech.blockkit.parser.span.Block
import io.adventech.blockkit.parser.span.Span
import io.adventech.blockkit.parser.span.TextAttributes
import kotlinx.collections.immutable.toImmutableList

@Stable
class SpanProcessor {

    /**
     * Processes a markdown string into a [Block], correctly
     * separating custom styled text from standard markdown content and handling
     * wrapping attribute markers.
     */
    fun process(markdown: String): Block {
        var cleanMarkdown = markdown
        var blockAttributes: TextAttributes? = null

        // --- Step 1: Only extract block attributes if they wrap the entire string ---
        // Check bold wrapping
        BOLD_REGEX.matchEntire(cleanMarkdown)?.let {
            val leadingSpaces = it.groupValues[1]
            val innerText = it.groupValues[3]
            val trailingSpaces = it.groupValues[5]

            blockAttributes = TextAttributes(bold = true)
            cleanMarkdown = leadingSpaces + innerText + trailingSpaces
        }

        // Check italic wrapping
        ITALIC_REGEX.matchEntire(cleanMarkdown)?.let {
            val leadingSpaces = it.groupValues[1]
            val innerText = it.groupValues[3]
            val trailingSpaces = it.groupValues[5]

            blockAttributes = blockAttributes?.merge(TextAttributes(italic = true)) ?: TextAttributes(italic = true)
            cleanMarkdown = leadingSpaces + innerText + trailingSpaces
        }

        // Check strikethrough wrapping
        STRIKETHROUGH_REGEX.matchEntire(cleanMarkdown)?.let {
            val leadingSpaces = it.groupValues[1]
            val innerText = it.groupValues[2]
            val trailingSpaces = it.groupValues[3]

            blockAttributes = blockAttributes?.merge(TextAttributes(strikethrough = true)) ?: TextAttributes(strikethrough = true)
            cleanMarkdown = leadingSpaces + innerText + trailingSpaces
        }

        // --- Step 2: Tokenize the remaining string into segments ---
        val finalSpans = resolveSegments(cleanMarkdown)

        // --- Step 3: Return Block with block-level attributes ---
        return Block(spans = finalSpans.toImmutableList(), attributes = blockAttributes)
    }

    // Internal token representation
    private data class Token(
        val text: String,
        val type: TokenType,
        val styledJson: String? = null,
    )

    private enum class TokenType { TEXT, DELIMITER, STYLED, STANDARD_LINK }

    /**
     * Turn the input into a list of Tokens where STYLED and STANDARD_LINK tokens are atomic
     * and never get split by delimiter regexes.
     */
    private fun tokenizePreservingStyled(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var lastIndex = 0

        // 1. First pass: Tokenize STYLED matches
        val styledMatches = STYLED_REGEX.findAll(input).toList()
        for (m in styledMatches) {
            val start = m.range.first
            val end = m.range.last + 1

            // Add plain slice before this styled match
            if (start > lastIndex) {
                tokens.add(Token(input.substring(lastIndex, start), TokenType.TEXT))
            }

            // Add the styled token as atomic
            val styledText = m.groupValues[1] // inner text
            // Trim Group 2 to ensure a clean JSON string is stored
            val json = m.groupValues[2].trim()
            tokens.add(Token(styledText, TokenType.STYLED, styledJson = json))

            lastIndex = end
        }

        // Add remaining tail (if any)
        if (lastIndex < input.length) {
            tokens.add(Token(input.substring(lastIndex), TokenType.TEXT))
        }

        // 2. Intermediate pass: Tokenize STANDARD_LINK matches within existing TEXT tokens
        val tokensWithLinks = mutableListOf<Token>()
        val standardLinkRegex = STANDARD_LINK_REGEX

        for (t in tokens) {
            if (t.type != TokenType.TEXT) {
                tokensWithLinks.add(t) // STYLED tokens pass through
                continue
            }

            val text = t.text
            var cursor = 0
            standardLinkRegex.findAll(text).forEach { m ->
                val s = m.range.first
                val e = m.range.last + 1

                // Add plain text before the link
                if (s > cursor) {
                    tokensWithLinks.add(Token(text.substring(cursor, s), TokenType.TEXT))
                }

                // Add the entire link as an atomic STANDARD_LINK token
                // m.value contains the full link string (e.g., "[text](url)")
                tokensWithLinks.add(Token(m.value, TokenType.STANDARD_LINK))
                cursor = e
            }

            // Add remaining tail text
            if (cursor < text.length) {
                tokensWithLinks.add(Token(text.substring(cursor), TokenType.TEXT))
            }
        }

        // 3. Final pass: Split remaining pure TEXT tokens by delimiters
        val finalTokens = mutableListOf<Token>()
        val delimiterRegex = ALL_DELIMITERS_REGEX

        for (t in tokensWithLinks) {
            if (t.type != TokenType.TEXT) {
                finalTokens.add(t) // STYLED and STANDARD_LINK pass through unchanged
                continue
            }

            val text = t.text
            var cursor = 0
            delimiterRegex.findAll(text).forEach { d ->
                val s = d.range.first
                val e = d.range.last + 1
                if (s > cursor) {
                    finalTokens.add(Token(text.substring(cursor, s), TokenType.TEXT))
                }
                finalTokens.add(Token(d.value, TokenType.DELIMITER))
                cursor = e
            }
            if (cursor < text.length) {
                finalTokens.add(Token(text.substring(cursor), TokenType.TEXT))
            }
        }

        // Filter empty text tokens
        return finalTokens.filter { !(it.type == TokenType.TEXT && it.text.isEmpty()) }
    }

    /**
     * Produce the final Spans from the token stream.
     */
    private fun resolveSegments(text: String): List<Span> {
        val tokens = tokenizePreservingStyled(text)
        val spans = mutableListOf<Span>()
        var currentAttributes = TextAttributes()

        for (token in tokens) {
            when (token.type) {
                TokenType.DELIMITER -> {
                    currentAttributes = toggleAttribute(currentAttributes, token.text)
                }

                TokenType.TEXT -> {
                    // Plain markdown text — preserve spaces exactly and attach current attributes (if any)
                    val attrs = if (currentAttributes.hasAttributes()) currentAttributes else null
                    // Re-adding ZWSP guards to prevent CommonMark from cross-boundary parsing
                    spans.add(Span.Markdown(text = "$ZERO_WIDTH_SPACE${token.text}$ZERO_WIDTH_SPACE", attributes = attrs))
                }

                TokenType.STANDARD_LINK -> {
                    // Standard Markdown Link (treated as atomic TEXT for CommonMark)
                    val attrs = if (currentAttributes.hasAttributes()) currentAttributes else null
                    // Re-adding ZWSP guards to prevent CommonMark from cross-boundary parsing
                    spans.add(Span.Markdown(text = "$ZERO_WIDTH_SPACE${token.text}$ZERO_WIDTH_SPACE", attributes = attrs))
                }

                TokenType.STYLED -> {
                    // token.text is the inner styled text (preserve spacing)
                    // token.styledJson is the JSON payload
                    val inner = token.text
                    val json = token.styledJson ?: "{}"

                    // Extract inner attributes (non-destructive).
                    val innerAttributesResult = extractAttributesAndCleanText(inner)

                    // Merge surrounding attributes (currentAttributes) with inner attributes
                    var merged = currentAttributes
                    if (innerAttributesResult.attributes != null) {
                        merged = merged.merge(innerAttributesResult.attributes)
                    }

                    val finalAttrs = if (merged.hasAttributes()) merged else null

                    // Emit the styled span using the ORIGINAL inner text (preserved)
                    spans.add(
                        Span.StyledMarkdown(
                            text = innerAttributesResult.text,
                            json = json,
                            attributes = finalAttrs
                        )
                    )
                }
            }
        }

        return spans.filterNot { it is Span.Markdown && it.text.isEmpty() }
    }

    /**
     * Toggles the specific boolean attribute based on the delimiter marker.
     */
    private fun toggleAttribute(attributes: TextAttributes, marker: String): TextAttributes {
        return when {
            MARKER_BOLD_REGEX.matches(marker) -> attributes.copy(bold = !attributes.bold)
            MARKER_ITALIC_REGEX.matches(marker) -> attributes.copy(italic = !attributes.italic)
            MARKER_STRIKETHROUGH_REGEX.matches(marker) -> attributes.copy(strikethrough = !attributes.strikethrough)
            else -> attributes
        }
    }

    // --- Only detect delimiter attrs if the ENTIRE string is wrapped ---
    private fun extractAttributesAndCleanText(text: String): TextAttributeResult {
        var collectedAttributes: TextAttributes? = null

        BOLD_REGEX.matchEntire(text)?.let {
            collectedAttributes = TextAttributes(bold = true)
        }

        ITALIC_REGEX.matchEntire(text)?.let {
            collectedAttributes = collectedAttributes?.merge(TextAttributes(italic = true)) ?: TextAttributes(italic = true)
        }

        STRIKETHROUGH_REGEX.matchEntire(text)?.let {
            collectedAttributes = collectedAttributes?.merge(TextAttributes(strikethrough = true)) ?: TextAttributes(strikethrough = true)
        }

        return TextAttributeResult(
            attributes = collectedAttributes,
            text = text,
        )
    }

    companion object {
        // Custom Styled Text: ^[text](json)
        // Group 1: inner text
        // Group 2: JSON payload including surrounding optional whitespace (\s*\{.*?\}\s*)
        private val STYLED_REGEX = Regex("""\^\[(.*?)\]\(\s*(\{.*?\}\s*)\)""")

        // Capture optional leading/trailing spaces outside the markers
        private val BOLD_REGEX = Regex("""(\s*)(\*\*|__)(.*?)(\*\*|__)(\s*)""")
        private val ITALIC_REGEX = Regex("""(\s*)(\*|_)(.*?)(\*|_)(\s*)""")
        private val STRIKETHROUGH_REGEX = Regex("""(\s*)~~(.*?)~~(\s*)""")

        // Standard Link: !?[text](url) - Captures the entire link structure
        private val STANDARD_LINK_REGEX = Regex("""(!?\[(.*?)\]\((.*?)\))""")

        // Regex for splitting by all attribute delimiters, capturing them
        private val ALL_DELIMITERS_REGEX = Regex("(\\*\\*|__|[*_]|~~)")
        private val MARKER_BOLD_REGEX = Regex("""\*\*|__""")
        private val MARKER_ITALIC_REGEX = Regex("""\*|_""")
        private val MARKER_STRIKETHROUGH_REGEX = Regex("""~~""")

        private const val ZERO_WIDTH_SPACE = "\u200B"
    }
}

private data class TextAttributeResult(val attributes: TextAttributes? = null, val text: String)
