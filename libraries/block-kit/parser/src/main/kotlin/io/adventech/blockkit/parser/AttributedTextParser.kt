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

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.adventech.blockkit.model.AttributedText
import io.adventech.blockkit.model.StyleContainer

interface AttributedTextParserDelegate {
    fun parse(markdown: String): List<AttributedText>
}

class AttributedTextParser() : AttributedTextParserDelegate {

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    override fun parse(markdown: String): List<AttributedText> {
        val attributes = mutableListOf<AttributedText>()
        val styleAdapter = moshi.adapter(StyleContainer::class.java)
        parseAttributedText(markdown)
            .forEach { text ->
                when (text) {
                    is AttributedTextType.Plain -> {
                        attributes.add(AttributedText.Plain(text.label))
                    }

                    is AttributedTextType.Styled -> {
                        val style = styleAdapter.fromJson(text.style) ?: return@forEach
                        attributes.add(
                            AttributedText.Styled(
                                label = text.label,
                                style = style.style.text
                            )
                        )
                    }
                }

            }

        return attributes
    }

    private sealed interface AttributedTextType {
        data class Plain(val label: String) : AttributedTextType
        data class Styled(val label: String, val style: String) : AttributedTextType
    }

    private fun parseAttributedText(input: String): List<AttributedTextType> {
        val regex = Regex("\\^\\[(.+?)]\\((.+?)\\)") // Matches `^[Label](style-json)`
        val result = mutableListOf<AttributedTextType>()

        var currentIndex = 0 // Tracks the current position in the input string

        regex.findAll(input).forEach { match ->
            // Extract the text before the current match as plain text
            val startIndex = match.range.first
            if (startIndex > currentIndex) {
                val plainText = input.substring(currentIndex, startIndex).trim()
                if (plainText.isNotEmpty()) {
                    result.add(AttributedTextType.Plain(plainText))
                }
            }

            // Extract the styled text
            val label = match.groupValues[1] // The label inside [Label]
            val style = match.groupValues[2] // The style inside (style-json)
            result.add(AttributedTextType.Styled(label, unescapeJsonString(style)))

            // Update the current index to the end of the current match
            currentIndex = match.range.last + 1
        }

        // Add any remaining plain text after the last match
        if (currentIndex < input.length) {
            val remainingText = input.substring(currentIndex).trim()
            if (remainingText.isNotEmpty()) {
                result.add(AttributedTextType.Plain(remainingText))
            }
        }

        return result
    }

    private fun unescapeJsonString(escapedJson: String): String {
        return escapedJson.replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\/", "/")
    }
}
