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
import io.adventech.blockkit.model.TitleTextStyle
import org.commonmark.node.Node
import org.commonmark.node.Text

interface AttributedTextParserDelegate {
    fun replaceNode(node: Text): Node

    fun parseTypeface(markdown: String): Set<String>
}

class AttributedTextParser() : AttributedTextParserDelegate {

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    override fun replaceNode(node: Text): Node {
        val regex = Regex(REGEX)
        val matchResult = regex.find(node.literal)

        if (matchResult != null) {
            val text = matchResult.groups[1]?.value ?: return node
            val attributesString = matchResult.groups[2]?.value ?: return node

            val style = parseJsonStyle(attributesString)?.text ?: return node

            return InlineAttributeNode(text, style)
        }

        return node
    }

    private fun parseJsonStyle(json: String): TitleTextStyle? {
        val styleAdapter = moshi.adapter(StyleContainer::class.java)
        val style = styleAdapter.fromJson(json)
        return style?.style
    }

    override fun parseTypeface(markdown: String): Set<String> {
        val regex = Regex(REGEX)
        val result = mutableListOf<String>()

        regex.findAll(markdown).forEach { match ->
            // Extract the styled text
            val style = match.groupValues[2] // The style inside (style-json)

            val textStyle = parseJsonStyle(style)
            textStyle?.text?.typeface?.let { result.add(it) }
        }

        return result.toSet()
    }

    companion object {
        // Matches `^[text](style-json)`
        private const val REGEX = "\\^\\[(.*?)]\\((\\s*?\\{.+?\\}\\s*?)\\)"
    }
}
