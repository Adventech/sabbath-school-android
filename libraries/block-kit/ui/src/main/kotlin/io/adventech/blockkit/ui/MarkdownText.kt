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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.adventech.blockkit.model.TextStyleOffset
import io.adventech.blockkit.model.TextStyleSize
import io.adventech.blockkit.parser.AttributedTextParser
import io.adventech.blockkit.ui.style.BlockStyleTemplate
import io.adventech.blockkit.ui.style.StyleTemplate
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import io.adventech.blockkit.ui.style.parse
import org.commonmark.node.Code
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
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
    overflow: TextOverflow = TextOverflow.Clip,
    onHandleUri: (String) -> Unit = {},
) {
    val styledText = rememberMarkdownText(markdownText, style, styleTemplate, color)

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
        overflow = overflow,
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

@Composable
internal fun rememberMarkdownText(
    markdownText: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    styleTemplate: StyleTemplate = BlockStyleTemplate.DEFAULT,
    color: Color = MaterialTheme.colorScheme.onSurface,
): AnnotatedString {
    val attributedTextParser by remember { mutableStateOf(AttributedTextParser()) }
    val typefaces by remember(markdownText) { mutableStateOf(attributedTextParser.parseTypeface(markdownText)) }
    var fonts = typefaces.associate { name -> name to LocalFontFamilyProvider.current.invoke(name) }
    val defaultFontFamily = Styler.defaultFontFamily()
    val fontProvider: (String?) -> FontFamily = {
        it?.let { fonts[it] } ?: defaultFontFamily
    }
    val fontSizeProvider: (TextStyleSize?) -> TextUnit = {
        styleTemplate.defaultTextSizePoints(it).sp
    }

    val parsedNode = remember(markdownText) {
        val parser = Parser.builder().build()
        parser.parse(markdownText) as Document
    }
    return buildAnnotatedString {
        withStyle(style.toSpanStyle()) {
            appendMarkdownChildren(parsedNode, color, attributedTextParser, fontProvider, fontSizeProvider)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun AnnotatedString.Builder.appendMarkdownChildren(
    parent: Node,
    color: Color,
    parser: AttributedTextParser,
    fontProvider: (String?) -> FontFamily,
    fontSizeProvider: (TextStyleSize?) -> TextUnit,
) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is Paragraph -> {
                appendMarkdownChildren(child, color, parser, fontProvider, fontSizeProvider)
            }

            is Text -> {
                appendText(child, parser, fontProvider, fontSizeProvider)
            }

            is Image -> appendInlineContent(TAG_IMAGE_URL, child.destination)
            is Emphasis -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendMarkdownChildren(child, color, parser, fontProvider, fontSizeProvider)
                }
            }

            is StrongEmphasis -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendMarkdownChildren(child, color, parser, fontProvider, fontSizeProvider)
                }
            }

            is Code -> {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    append(child.literal)
                }
            }

            is HardLineBreak -> {
                append("\n")
            }

            is Link -> {
                withStyle(SpanStyle(color, textDecoration = TextDecoration.Underline)) {
                    withAnnotation(TAG_URL, child.destination) {
                        appendMarkdownChildren(child, color, parser, fontProvider, fontSizeProvider)
                    }
                }
            }

            is Heading -> {
                val fontSize = fontSizeProvider(textStyleFromLevel(child.level))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = fontSize)) {
                    appendMarkdownChildren(child, color, parser, fontProvider, fontSizeProvider)
                }
                appendLine()
            }
        }
        child = child.next
    }
}

private fun textStyleFromLevel(level: Int): TextStyleSize {
    return when (level) {
        1 -> TextStyleSize.XL
        2 -> TextStyleSize.LG
        3 -> TextStyleSize.BASE
        4 -> TextStyleSize.SM
        5 -> TextStyleSize.XS
        else -> TextStyleSize.BASE
    }
}

/**
 * Append text to the [AnnotatedString.Builder] with custom style handling.
 *
 * @param text The text node to append.
 * @param parser The attributed text parser to use for parsing custom styles.
 * @param fontProvider The font provider to use for custom fonts.
 * @param fontSizeProvider The font size provider to use for custom font sizes.
 */
private fun AnnotatedString.Builder.appendText(
    text: Text,
    parser: AttributedTextParser,
    fontProvider: (String?) -> FontFamily,
    fontSizeProvider: (TextStyleSize?) -> TextUnit,
) {
    val markdown = text.literal
    var lastIndex = 0
    parser.findAllMatches(markdown).forEach { matchResult ->
        val matchStart = matchResult.range.first
        val matchEnd = matchResult.range.last
        val text = matchResult.groups[1]?.value ?: ""
        val styleJson = matchResult.groups[2]?.value ?: ""

        // Append text before the match
        if (matchStart > lastIndex) {
            append(markdown.substring(lastIndex, matchStart))
        }

        // Extract inline text style from JSON
        val inlineTextStyle = parser.parseJsonStyle(styleJson)
        if (inlineTextStyle != null) {
            withStyle(inlineTextStyle.toSpanStyle(fontProvider, fontSizeProvider)) {
                append(text)
            }
        } else {
            append(text)
        }

        lastIndex = matchEnd + 1
    }

    // Append remaining text after the last match
    if (lastIndex < markdown.length) {
        append(markdown.substring(lastIndex))
    }
}

private fun io.adventech.blockkit.model.TextStyle.toSpanStyle(
    fontProvider: (String?) -> FontFamily,
    fontSizeProvider: (TextStyleSize?) -> TextUnit
): SpanStyle {
    val fontSize = fontSizeProvider(size)
    return SpanStyle(
        color = color?.let { Color.parse(it) } ?: Color.Unspecified,
        fontFamily = fontProvider(typeface),
        fontSize = if (offset == null) fontSize else fontSize * 0.70f,
        baselineShift = offset?.toBaselineShift()
    )
}

private fun TextStyleOffset.toBaselineShift(): BaselineShift? {
    return when (this) {
        TextStyleOffset.SUP -> BaselineShift(0.5f)
        TextStyleOffset.SUB -> BaselineShift(-0.5f)
        TextStyleOffset.UNKNOWN -> null
    }
}
