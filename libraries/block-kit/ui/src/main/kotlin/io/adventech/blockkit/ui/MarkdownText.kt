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

package io.adventech.blockkit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.adventech.blockkit.model.TextStyleSize
import io.adventech.blockkit.parser.AttributedText
import io.adventech.blockkit.parser.AttributedTextParser
import io.adventech.blockkit.ui.style.BlockStyleTemplate
import io.adventech.blockkit.ui.style.LatoFontFamily
import io.adventech.blockkit.ui.style.StyleTemplate
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import io.adventech.blockkit.ui.style.parse
import org.commonmark.node.Code
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.parser.Parser

internal const val TAG_URL = "url"
internal const val TAG_IMAGE_URL = "imageUrl"

@Composable
fun MarkdownText(
    markdownText: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    textAlign: TextAlign? = null,
    styleTemplate: StyleTemplate = BlockStyleTemplate.DEFAULT,
    maxLines: Int = Integer.MAX_VALUE,
    onHandleUri: (String) -> Unit = {},
) {
    val attributedTextParser by remember { mutableStateOf(AttributedTextParser()) }
    val attributes by remember(markdownText) { mutableStateOf(attributedTextParser.parse(markdownText)) }
    var fonts = attributes.associate { attr ->
        when (attr) {
            is AttributedText.Plain -> {
                null to null
            }
            is AttributedText.Styled -> {
                attr.style?.typeface to attr.style?.typeface?.let { LocalFontFamilyProvider.current.invoke(it) }
            }
        }

    }
    val fontProvider: (String?) -> FontFamily = {
        it?.let { fonts[it] } ?: LatoFontFamily
    }
    val fontSizeProvider: (TextStyleSize?) -> TextUnit = {
        styleTemplate.defaultTextSizePoints(it).sp
    }

    val styledText = buildAnnotatedString {
        if (attributes.fastAny { it is AttributedText.Styled }) {
            pushStyle(style.toSpanStyle())
            appendAttributedText(attributes, fontProvider, fontSizeProvider, color)
        } else {
            val parsedNode = remember(markdownText) {
                val parser = Parser.builder().build()
                val root = parser.parse(markdownText) as Document
                root.firstChild
            }
            pushStyle(style.toSpanStyle())
            appendMarkdownChildren(parsedNode, color)
            pop()
        }
    }

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = styledText,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    layoutResult.value?.let { layoutResult ->
                        val position = layoutResult.getOffsetForPosition(offset)
                        styledText
                            .getStringAnnotations(position, position)
                            .firstOrNull()
                            ?.let { annotation ->
                                if (annotation.tag == TAG_URL) {
                                    onHandleUri(annotation.item)
                                }
                            }
                    }
                }
            },
        color = color,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        inlineContent = mapOf(
            TAG_IMAGE_URL to InlineTextContent(
                Placeholder(style.fontSize, style.fontSize, PlaceholderVerticalAlign.Bottom)
            ) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it)
                        .crossfade(true)
                        .build()
                )

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier,
                    alignment = Alignment.Center
                )
            }
        ),
        onTextLayout = { layoutResult.value = it },
    )
}

internal fun AnnotatedString.Builder.appendMarkdownChildren(
    parent: Node, color: Color,
) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is Paragraph -> appendMarkdownChildren(child, color)
            is Text -> append(child.literal)
            is Image -> appendInlineContent(TAG_IMAGE_URL, child.destination)
            is Emphasis -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendMarkdownChildren(child, color)
                pop()
            }

            is StrongEmphasis -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendMarkdownChildren(child, color)
                pop()
            }

            is Code -> {
                pushStyle(TextStyle(fontFamily = FontFamily.Monospace).toSpanStyle())
                append(child.literal)
                pop()
            }

            is HardLineBreak -> {
                append("\n")
            }

            is Link -> {
                val underline = SpanStyle(color, textDecoration = TextDecoration.Underline)
                pushStyle(underline)
                pushStringAnnotation(TAG_URL, child.destination)
                appendMarkdownChildren(child, color)
                pop()
                pop()
            }
        }
        child = child.next
    }
}

private fun AnnotatedString.Builder.appendAttributedText(
    attributes: List<AttributedText>,
    fontProvider: (String?) -> FontFamily,
    fontSizeProvider: (TextStyleSize?) -> TextUnit,
    color: Color
) {
    attributes.forEachIndexed { index, attribute ->
        when (attribute) {
            is AttributedText.Plain -> {
                if (attribute.label == "\\") {
                    appendLine()
                } else {
                    val node = Parser.builder().build().parse(attribute.label)
                    appendMarkdownChildren(node.firstChild, color)
                }
            }

            is AttributedText.Styled -> {
                attribute.style?.toSpanStyle(fontProvider, fontSizeProvider)?.let { pushStyle(it) }
                val node = Parser.builder().build().parse(attribute.label)
                appendMarkdownChildren(node.firstChild, color)
                pop()
            }
        }
    }
}

private fun io.adventech.blockkit.model.TextStyle.toSpanStyle(
    fontProvider: (String?) -> FontFamily,
    fontSizeProvider: (TextStyleSize?) -> TextUnit
): SpanStyle {
    return SpanStyle(
        color = color?.let { Color.parse(it) } ?: Color.Unspecified,
        fontFamily = fontProvider(typeface),
        fontSize = fontSizeProvider(size),
        // Apply BaselineShift from TextStyleOffset
    )
}
