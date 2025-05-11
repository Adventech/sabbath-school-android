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

package io.adventech.blockkit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import io.adventech.blockkit.model.input.Highlight
import io.adventech.blockkit.parser.AttributedTextParser
import io.adventech.blockkit.ui.color.AttributedTextColorOverride
import io.adventech.blockkit.ui.color.parse
import io.adventech.blockkit.ui.color.toColor
import io.adventech.blockkit.ui.style.BlockStyleTemplate
import io.adventech.blockkit.ui.style.StyleTemplate
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import io.adventech.blockkit.ui.style.rememberAttributedTextColorOverride
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import me.saket.extendedspans.ExtendedSpans
import me.saket.extendedspans.RoundedCornerSpanPainter
import me.saket.extendedspans.RoundedCornerSpanPainter.TextPaddingValues
import me.saket.extendedspans.drawBehind
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
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
    highlights: ImmutableList<Highlight> = persistentListOf(),
    onHandleUri: (String) -> Unit = {},
) {
    val styledText = rememberMarkdownText(markdownText, style, styleTemplate, color, highlights)

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val extendedSpans = remember {
        ExtendedSpans(
            RoundedCornerSpanPainter(
                cornerRadius = 6.sp,
                padding = TextPaddingValues(horizontal = 4.sp),
                topMargin = 2.sp,
                bottomMargin = 2.sp,
                stroke = RoundedCornerSpanPainter.Stroke(
                    color = Color.Transparent,
                ),
            )
        )
    }

    Text(
        text = remember(styledText) {
            extendedSpans.extend(styledText)
        },
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
            }
            .drawBehind(extendedSpans),
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
        onTextLayout = {
            layoutResult.value = it
            extendedSpans.onTextLayout(it)
        },
    )
}

@Composable
internal fun rememberMarkdownText(
    markdownText: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    styleTemplate: StyleTemplate = BlockStyleTemplate.DEFAULT,
    color: Color = MaterialTheme.colorScheme.onSurface,
    highlights: ImmutableList<Highlight> = persistentListOf(),
    attributedTextParser: AttributedTextParser = remember { AttributedTextParser() }
): AnnotatedString {
    val defaultFontFamily = Styler.defaultFontFamily()
    val fonts by rememberMarkdownFonts(markdownText, attributedTextParser, defaultFontFamily)
    val fontProvider: (String?) -> FontFamily = remember(fonts) { { it?.let { fonts[it] } ?: defaultFontFamily } }
    val fontSizeProvider: (TextStyleSize?) -> TextUnit = remember { { styleTemplate.defaultTextSizePoints(it).sp } }
    val attributedTextColorOverride = rememberAttributedTextColorOverride(color, styleTemplate)

    val parser = remember { Parser.builder().build() }
    val parsedNode = remember(markdownText) { parser.parse(markdownText) as Document }

    return remember(parsedNode, style, color, attributedTextColorOverride, highlights, fontProvider, fontSizeProvider) {
        buildAnnotatedString {
            withStyle(style.toSpanStyle()) {
                appendMarkdownChildren(parsedNode, color, attributedTextColorOverride, style.fontSize, attributedTextParser, fontProvider, fontSizeProvider)
            }

            // Apply highlights to the result text
            highlights.forEach { highlight ->
                // Ensure indices are within valid bounds of the text.
                val textLength = this.length
                if (highlight.startIndex in 0 until textLength && highlight.endIndex in (highlight.startIndex + 1)..textLength) {
                    addStyle(
                        style = SpanStyle(background = highlight.color.toColor()),
                        start = highlight.startIndex,
                        end = highlight.endIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberMarkdownFonts(
    markdownText: String,
    attributedTextParser: AttributedTextParser,
    defaultFontFamily: FontFamily,
): State<Map<String, FontFamily>> {
    val typefaces = remember(markdownText) { attributedTextParser.parseTypeface(markdownText) }
    val provider = LocalFontFamilyProvider.current

    return produceState(emptyMap(), typefaces) {
        getCombinedFontFamilies(typefaces, provider, defaultFontFamily).collect { fontFamilies ->
            val fontFamilyMap = mutableMapOf<String, FontFamily>()
            typefaces.zip(fontFamilies).forEach { (name, fontFamily) ->
                fontFamilyMap[name] = fontFamily // Add to the map
            }
            value = fontFamilyMap
        }
    }
}

private fun getCombinedFontFamilies(
    fontNames: Set<String>,
    provider: FontFamilyProvider,
    defaultFontFamily: FontFamily,
): Flow<List<FontFamily>> {
    return fontNames
        .map { fontName -> provider(fontName, defaultFontFamily) } // Get individual flows for each font name
        .let { fontFlows -> combine(fontFlows) { it.toList() } } // Combine them into a single Flow
}

private fun AnnotatedString.Builder.appendMarkdownChildren(
    parent: Node,
    color: Color,
    attrTextColorOverride: AttributedTextColorOverride,
    fontSize: TextUnit,
    parser: AttributedTextParser,
    fontProvider: (String?) -> FontFamily,
    fontSizeProvider: (TextStyleSize?) -> TextUnit,
) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is Paragraph -> {
                appendMarkdownChildren(child, color, attrTextColorOverride, fontSize, parser, fontProvider, fontSizeProvider)
            }

            is Text -> {
                appendText(child, fontSize, parser, attrTextColorOverride, fontProvider, fontSizeProvider)
            }

            is Image -> appendInlineContent(TAG_IMAGE_URL, child.destination)
            is Emphasis -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendMarkdownChildren(child, color, attrTextColorOverride, fontSize, parser, fontProvider, fontSizeProvider)
                }
            }

            is StrongEmphasis -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendMarkdownChildren(child, color, attrTextColorOverride, fontSize, parser, fontProvider, fontSizeProvider)
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
                        appendMarkdownChildren(child, color,attrTextColorOverride, fontSize, parser, fontProvider, fontSizeProvider)
                    }
                }
            }

            is Heading -> {
                val fontSize = fontSizeProvider(textStyleFromLevel(child.level))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = fontSize)) {
                    appendMarkdownChildren(child, color, attrTextColorOverride,fontSize, parser, fontProvider, fontSizeProvider)
                }
                appendLine()
            }

            is ListItem -> {
                appendMarkdownChildren(child, color, attrTextColorOverride,fontSize, parser, fontProvider, fontSizeProvider)
            }

            is ListBlock -> {
                when (child) {
                    is OrderedList -> {
                        append(child.startNumber.toString())
                        append(child.delimiter)
                        append(" ")
                    }

                    is BulletList -> {
                        append(child.bulletMarker)
                        append(" ")
                    }
                }
                appendMarkdownChildren(child.firstChild, color, attrTextColorOverride, fontSize, parser, fontProvider, fontSizeProvider)
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
    fontSize: TextUnit,
    parser: AttributedTextParser,
    attrTextColorOverride: AttributedTextColorOverride,
    fontProvider: (String?) -> FontFamily,
    fontSizeProvider: (TextStyleSize?) -> TextUnit,
) {
    val markdown = text.literal
    var lastIndex = 0

    // Find all URL matches
    val plainUrlMatches = urlRegex.findAll(markdown).toList()

    // Find all parser matches
    val attributedTextMatches = parser.findAllMatches(markdown).toList()

    // Combine URL matches and parser matches
    val allMatches = (attributedTextMatches + plainUrlMatches)
        .sortedBy { it.range.first } // Sort by start index

    allMatches.forEach { matchResult ->
        val matchStart = matchResult.range.first
        val matchEnd = matchResult.range.last

        // Append text before the match
        if (matchStart > lastIndex) {
            append(markdown.substring(lastIndex, matchStart))
        }

        when (matchResult) {
            in attributedTextMatches -> {
                // Handle parser matches
                val text = matchResult.groups[1]?.value ?: ""
                val styleJson = matchResult.groups[2]?.value ?: ""

                // Extract inline text style from JSON
                val inlineTextStyle = parser.parseJsonStyle(styleJson)
                if (inlineTextStyle != null) {
                    withStyle(inlineTextStyle.toSpanStyle(attrTextColorOverride, fontSize, fontProvider, fontSizeProvider)) {
                        append(text)
                    }
                } else {
                    append(text)
                }
            }
            in plainUrlMatches -> {
                // Handle URL matches
                val webUrl = matchResult.value
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    withAnnotation(TAG_URL, webUrl) {
                        append(webUrl)
                    }
                }
            }
        }

        lastIndex = matchEnd + 1
    }

    // Append remaining text after the last match
    if (lastIndex < markdown.length) {
        append(markdown.substring(lastIndex))
    }
}

// Regex to detect plain web URLs
private val urlRegex = Regex("""(https?://|www\.)\S+[^!:,.;\s]""")

private fun io.adventech.blockkit.model.TextStyle.toSpanStyle(
    attributedTextColorOverride: AttributedTextColorOverride,
    defaultFontSize: TextUnit,
    fontProvider: (String?) -> FontFamily,
    fontSizeProvider: (TextStyleSize?) -> TextUnit
): SpanStyle {
    val color = when (attributedTextColorOverride) {
        is AttributedTextColorOverride.CustomColor -> color?.let { attributedTextColorOverride.color } ?: Color.Unspecified
        AttributedTextColorOverride.None -> color?.let { Color.parse(it) } ?: Color.Unspecified
    }
    val fontSize = size?.let {
        fontSizeProvider(it) * if (offset == null) 1.0f else OFFSET_MULTIPLIER
    } ?: defaultFontSize.takeUnless { offset == null }?.let { it * OFFSET_MULTIPLIER }

    return SpanStyle(
        color = color,
        fontFamily = typeface?.let { fontProvider(typeface) },
        fontSize = fontSize ?: TextUnit.Unspecified,
        baselineShift = offset?.toBaselineShift(),
    )
}

private const val OFFSET_MULTIPLIER = 0.50f

private fun TextStyleOffset.toBaselineShift(): BaselineShift? {
    return when (this) {
        TextStyleOffset.SUP -> BaselineShift(0.3f)
        TextStyleOffset.SUB -> BaselineShift(-0.3f)
        TextStyleOffset.UNKNOWN -> null
    }
}
