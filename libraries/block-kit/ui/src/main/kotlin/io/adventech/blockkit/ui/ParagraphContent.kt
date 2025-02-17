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

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.input.Highlight
import io.adventech.blockkit.model.input.HighlightColor
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import io.adventech.blockkit.ui.input.MarkdownTextInput
import io.adventech.blockkit.ui.input.SelectionBlockContainer
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.input.find
import io.adventech.blockkit.ui.input.rememberContentHighlights
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ParagraphContent(
    blockItem: BlockItem.Paragraph,
    modifier: Modifier = Modifier,
    parent: BlockItem? = null,
    inputState: UserInputState? = null,
    onHandleUri: (String) -> Unit = {},
) {
    val isSelectable = remember(parent) { parent.isChildSelectable() }
    if (isSelectable) {
        SelectableParagraph(
            blockItem = blockItem,
            modifier = modifier,
            inputState = inputState,
            onHandleUri = onHandleUri,
        )
    } else {
        ReadOnlyParagraph(
            blockItem = blockItem,
            modifier = modifier,
            onHandleUri = onHandleUri,
        )
    }
}

@Composable
private fun ReadOnlyParagraph(
    blockItem: BlockItem.Paragraph,
    modifier: Modifier = Modifier,
    onHandleUri: (String) -> Unit = {},
) {
    val blockStyle = blockItem.style?.text

    SelectionContainer {
        MarkdownText(
            markdownText = blockItem.markdown,
            modifier = modifier,
            color = Styler.textColor(blockStyle),
            style = Styler.textStyle(blockStyle),
            textAlign = Styler.textAlign(blockStyle),
            onHandleUri = onHandleUri,
        )
    }
}

@Composable
private fun SelectableParagraph(
    blockItem: BlockItem.Paragraph,
    modifier: Modifier = Modifier,
    inputState: UserInputState? = null,
    onHandleUri: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val blockStyle = blockItem.style?.text

    val highlights = rememberContentHighlights(blockItem.id, inputState)
    var localHighlights by remember(highlights) { mutableStateOf(highlights) }

    var textFieldValue by remember { mutableStateOf<TextFieldValue?>(null) }
    val currentSelection = textFieldValue?.selection

    SelectionBlockContainer(
        selection = currentSelection,
        onHighlight = { highlight ->
            textFieldValue = textFieldValue?.copy(
                selection = TextRange.Zero,
            )
            val input: UserInput.Highlights? = inputState?.find(blockItem.id)
            val highlights = input?.highlights.orEmpty() + highlight
            val request = UserInputRequest.Highlights(
                blockId = blockItem.id,
                highlights = highlights
            )
            localHighlights = highlights.toImmutableList()
            inputState?.eventSink?.invoke(UserInputState.Event.InputChanged(request))
        },
        onRemoveHighlight = {
            // Remove any highlight who's startIndex and endIndex are in the range of the current selection
            val selection = currentSelection ?: return@SelectionBlockContainer
            val input: UserInput.Highlights? = inputState?.find(blockItem.id)
            val highlights = removeHighlightsInRange(input?.highlights.orEmpty(), selection)
            val request = UserInputRequest.Highlights(
                blockId = blockItem.id,
                highlights = highlights
            )
            localHighlights = highlights.toImmutableList()
            inputState?.eventSink?.invoke(UserInputState.Event.InputChanged(request))
            textFieldValue = textFieldValue?.copy(
                selection = TextRange.Zero,
            )
        },
        onSearchSelection = { selection ->
            val searchText = textFieldValue?.text?.substring(selection.min..selection.max) ?: return@SelectionBlockContainer
            textFieldValue = textFieldValue?.copy(
                selection = TextRange.Zero,
            )
            onSearchSelection(context, searchText)
        }
    ) {
        MarkdownTextInput(
            markdownText = blockItem.markdown,
            onValueChange = { textFieldValue = it },
            modifier = modifier,
            selection = currentSelection ?: TextRange.Zero,
            color = Styler.textColor(blockStyle),
            style = Styler.textStyle(blockStyle),
            textAlign = Styler.textAlign(blockStyle),
            onHandleUri = onHandleUri,
            highlights = localHighlights,
        )
    }
}

private fun removeHighlightsInRange(highlights: List<Highlight>, range: TextRange): List<Highlight> {
    return highlights.filterNot { highlight ->
        highlight.startIndex in range.min..range.max && highlight.endIndex in range.min..range.max ||
            highlight.startIndex <= range.max && highlight.endIndex >= range.min
    }
}

private fun BlockItem?.isChildSelectable(): Boolean {
    return when (this) {
        null -> true
        is BlockItem.Collapse,
        is BlockItem.Quote,
        is BlockItem.BlockListItem,
        is BlockItem.ExcerptItem,
        is BlockItem.TableBlock,
        is BlockItem.Question -> true
        else -> false
    }
}

private fun onSearchSelection(context: Context, query: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
    context.startActivity(intent)
}

@Composable
@PreviewLightDark
private fun Preview() {
    val blockId = "blockId"
    val inputState = remember {
        UserInputState(
            input = highlights.map {
                UserInput.Highlights(
                    blockId = blockId,
                    id = "",
                    timestamp = 0L,
                    highlights = highlights
                )
            }.toImmutableList(),
            bibleVersion = null,
            eventSink = {}
        )
    }
    BlocksPreviewTheme {
        Surface {
            ParagraphContent(
                blockItem = BlockItem.Paragraph(
                    id = blockId,
                    style = null,
                    data = null,
                    nested = null,
                    markdown = MARKDOWN
                ),
                modifier = Modifier.padding(16.dp),
                inputState = inputState,
            )
        }
    }
}

internal const val MARKDOWN =
    "Kotlin's **sealed interfaces** provide a structured way to represent restricted hierarchies. For example, if you're designing a UI state system, you might have states like `Loading`, `Fallback`, and `Navigation`. Unlike `sealed class`, a **sealed interface** allows multiple inheritance, making it more flexible. If you prefer an explicit approach, using `None` instead of nullable types can improve clarity. 🚀"
private val highlights = listOf(
    Highlight(startIndex = 9, endIndex = 26, length = 17, color = HighlightColor.BLUE),
    Highlight(startIndex = 89, endIndex = 111, length = 22, color = HighlightColor.YELLOW),
    Highlight(startIndex = 112, endIndex = 126, length = 14, color = HighlightColor.ORANGE),
    Highlight(startIndex = 163, endIndex = 296, length = 22, color = HighlightColor.GREEN)
)
