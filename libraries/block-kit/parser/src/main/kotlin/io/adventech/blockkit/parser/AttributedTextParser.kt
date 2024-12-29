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
import io.adventech.blockkit.model.StyleContainer
import io.adventech.blockkit.model.TextStyle
import timber.log.Timber

interface AttributedTextParserDelegate {
    fun findAllMatches(markdown: String): Sequence<MatchResult>

    fun parseJsonStyle(json: String): TextStyle?

    fun parseTypeface(markdown: String): Set<String>
}

class AttributedTextParser() : AttributedTextParserDelegate {

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    override fun findAllMatches(markdown: String): Sequence<MatchResult> {
        return REGEX.toRegex().findAll(markdown)
    }

    override fun parseJsonStyle(json: String): TextStyle? {
        try {
            val styleAdapter = moshi.adapter(StyleContainer::class.java)
            val style = styleAdapter.fromJson(json)
            return style?.style?.text
        } catch (exception: Exception) {
            Timber.e(exception)
            return null
        }
    }

    override fun parseTypeface(markdown: String): Set<String> {
        val result = mutableListOf<String>()

        findAllMatches(markdown).forEach { match ->
            // Extract the styled text
            val style = match.groupValues[2] // The style inside (style-json)

            val textStyle = parseJsonStyle(style)
            textStyle?.typeface?.let { result.add(it) }
        }

        return result.toSet()
    }

    companion object {
        // Matches `^[text](style-json)`
        private const val REGEX = "\\^\\[(.*?)]\\((\\s*?\\{.+?\\}\\s*?)\\)"
    }
}
