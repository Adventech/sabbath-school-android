/*
 * Copyright 2022 NAVER Webtoon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.ss.design.compose.widget.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

/**
 * Basic element that displays text with read more.
 * Typically you will instead want to use [ReadMoreText], which is
 * a higher level Text element that contains semantics and consumes style information from a theme.
 *
 * @param text The text to be displayed.
 * @param modifier [Modifier] to apply to this layout node.
 * @param style Style configuration for the text such as color, font, line height etc.
 * @param onTextLayout Callback that is executed when a new text layout is calculated. A
 * [TextLayoutResult] object that callback provides contains paragraph information, size of the
 * text, baselines and other details. The callback can be used to add additional decoration or
 * functionality to the text. For example, to draw selection around the text.
 * @param softWrap Whether the text should break at soft line breaks. If false, the glyphs in the
 * text will be positioned as if there was unlimited horizontal space. If [softWrap] is false,
 * TextAlign may have unexpected effects.
 * @param readMoreText The read more text to be displayed in the collapsed state.
 * @param readMoreMaxLines An optional maximum number of lines for the text to span, wrapping if
 * necessary. If the text exceeds the given number of lines, it will be truncated according to
 * [Typography.ellipsis]. If it is not null, then it must be greater than zero.
 * @param readMoreStyle Style configuration for the read more text such as color, font, line height
 * etc.
 */
@Composable
internal fun BasicReadMoreText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    softWrap: Boolean = true,
    readMoreText: String = "",
    readMoreMaxLines: Int = 2,
    readMoreStyle: SpanStyle = style.toSpanStyle()
) {
    require(readMoreMaxLines > 0) { "readMoreMaxLines should be greater than 0" }

    val overflowText: String = remember {
        buildString {
            append(Typography.ellipsis)
            if (readMoreText.isNotEmpty()) {
                append(Typography.nbsp)
            }
        }
    }
    val readMoreTextWithStyle: AnnotatedString = remember(readMoreText, readMoreStyle) {
        buildAnnotatedString {
            if (readMoreText.isNotEmpty()) {
                withStyle(readMoreStyle) {
                    append(readMoreText.replace(' ', Typography.nbsp))
                }
            }
        }
    }

    val state = remember(text, readMoreMaxLines) {
        ReadMoreState(
            originalText = AnnotatedString(text),
            readMoreMaxLines = readMoreMaxLines
        )
    }
    val collapsedText = state.collapsedText
    val currentText = buildAnnotatedString {
        if (collapsedText.isNotEmpty()) {
            append(collapsedText)
            append(overflowText)
            append(readMoreTextWithStyle)
        } else {
            append(text)
        }
    }
    Box(modifier = modifier) {
        BasicText(
            text = currentText,
            modifier = Modifier,
            style = style,
            onTextLayout = {
                state.onTextLayout(it)
                onTextLayout(it)
            },
            overflow = TextOverflow.Ellipsis,
            softWrap = softWrap,
            maxLines = readMoreMaxLines
        )
        BasicText(
            text = overflowText,
            onTextLayout = { state.onOverflowTextLayout(it) },
            modifier = Modifier.notDraw(),
            style = style
        )
        BasicText(
            text = readMoreTextWithStyle,
            onTextLayout = { state.onReadMoreTextLayout(it) },
            modifier = Modifier.notDraw(),
            style = style.merge(readMoreStyle)
        )
    }
}

private fun Modifier.notDraw(): Modifier {
    return then(NotDrawModifier)
}

private object NotDrawModifier : DrawModifier {

    override fun ContentDrawScope.draw() {
        // not draws content.
    }
}

// ////////////////////////////////////
// ReadMoreState
// ////////////////////////////////////

@Stable
private class ReadMoreState(
    private val originalText: AnnotatedString,
    private val readMoreMaxLines: Int
) {
    private var textLayout: TextLayoutResult? = null
    private var overflowTextLayout: TextLayoutResult? = null
    private var readMoreTextLayout: TextLayoutResult? = null

    private var _collapsedText: AnnotatedString by mutableStateOf(AnnotatedString(""))

    var collapsedText: AnnotatedString
        get() = _collapsedText
        internal set(value) {
            if (value != _collapsedText) {
                _collapsedText = value
            }
        }

    fun onTextLayout(result: TextLayoutResult) {
        val lastLineIndex = readMoreMaxLines - 1
        val previous = textLayout
        val old = previous != null &&
            previous.lineCount >= readMoreMaxLines &&
            previous.isLineEllipsized(lastLineIndex)
        val new = result.lineCount >= readMoreMaxLines &&
            result.isLineEllipsized(lastLineIndex)
        val changed = previous != result && old != new
        if (changed) {
            textLayout = result
            updateCollapsedText()
        }
    }

    fun onOverflowTextLayout(result: TextLayoutResult) {
        val changed = overflowTextLayout?.size?.width != result.size.width
        if (changed) {
            overflowTextLayout = result
            updateCollapsedText()
        }
    }

    fun onReadMoreTextLayout(result: TextLayoutResult) {
        val changed = readMoreTextLayout?.size?.width != result.size.width
        if (changed) {
            readMoreTextLayout = result
            updateCollapsedText()
        }
    }

    private fun updateCollapsedText() {
        val lastLineIndex = readMoreMaxLines - 1
        val textLayout = textLayout
        val overflowTextLayout = overflowTextLayout
        val readMoreTextLayout = readMoreTextLayout
        if (textLayout != null &&
            overflowTextLayout != null &&
            readMoreTextLayout != null &&
            textLayout.lineCount >= readMoreMaxLines &&
            textLayout.isLineEllipsized(lastLineIndex)
        ) {
            val countUntilMaxLine = textLayout.getLineEnd(readMoreMaxLines - 1, visibleEnd = true)
            val readMoreWidth = overflowTextLayout.size.width + readMoreTextLayout.size.width
            val maximumWidth = textLayout.size.width - readMoreWidth
            var replacedEndIndex = countUntilMaxLine + 1
            var currentTextBounds: Rect
            do {
                replacedEndIndex -= 1
                currentTextBounds = textLayout.getCursorRect(replacedEndIndex)
            } while (currentTextBounds.left > maximumWidth)
            collapsedText = originalText.subSequence(startIndex = 0, endIndex = replacedEndIndex)
        }
    }

    override fun toString(): String {
        return "ReadMoreState(" +
            "originalText=$originalText, " +
            "readMoreMaxLines=$readMoreMaxLines, " +
            "collapsedText=$collapsedText" +
            ")"
    }
}
