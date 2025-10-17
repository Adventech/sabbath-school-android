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

package io.adventech.blockkit.ui.input

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.foundation.text.contextmenu.modifier.filterTextContextMenuComponents
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.core.net.toUri
import io.adventech.blockkit.model.input.Highlight
import io.adventech.blockkit.model.input.HighlightColor
import io.adventech.blockkit.model.input.Underline
import app.ss.translations.R as L10nR
import io.adventech.blockkit.ui.R as BlockKitR

/**
 * Composable container for handling text selection actions such as highlight, underline, and search.
 *
 * @param selection The current text selection range.
 * @param modifier Modifier to be applied to the container.
 * @param onHighlight Callback when a highlight is applied.
 * @param onRemoveHighlight Callback when a highlight is removed.
 * @param onSearchSelection Callback when a search is triggered for the selection.
 * @param onUnderLine Callback when an underline is applied.
 * @param onRemoveUnderline Callback when an underline is removed.
 * @param content Composable content to be displayed inside the container.
 */
@Composable
internal fun SelectionBlockContainer(
    selection: TextRange,
    modifier: Modifier = Modifier,
    onHighlight: (Highlight) -> Unit = {},
    onRemoveHighlight: () -> Unit = {},
    onSearchSelection: (TextRange) -> Unit = {},
    onUnderLine: (Underline) -> Unit = {},
    onRemoveUnderline: () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    val context = LocalContext.current
    val canSearchSelection = remember { context.isBrowserInstalled() }
    var contextMenuState by remember { mutableStateOf(ContextMenuType.DEFAULT) }
    val currSelection by rememberUpdatedState(selection)

    val onHighlight: (HighlightColor) -> Unit = { color ->
        currSelection.takeIf { it.length > 0 }?.let { selection ->
            val (startIndex, endIndex) = if (selection.end < selection.start) {
                selection.end to selection.start
            } else {
                selection.start to selection.end
            }

            onHighlight(
                Highlight(
                    startIndex = startIndex,
                    endIndex = endIndex,
                    length = selection.length,
                    color = color,
                )
            )
        }
    }

    val onRemoveHighlight = {
        if (currSelection.length > 0) {
            onRemoveHighlight()
        }
    }

    val onUnderline: (HighlightColor) -> Unit =
        { color ->
            currSelection.takeIf { it.length > 0 }?.let { selection ->
                val (startIndex, endIndex) = if (selection.end < selection.start) {
                    selection.end to selection.start
                } else {
                    selection.start to selection.end
                }

                onUnderLine(
                    Underline(
                        startIndex = startIndex,
                        endIndex = endIndex,
                        length = selection.length,
                        color = color,
                    )
                )
            }
        }

    val onRemoveUnderline =
        {
            if (currSelection.length > 0) {
                onRemoveUnderline()
            }
        }

    val onSearch =
        if (canSearchSelection) {
            {
                currSelection.takeIf { it.length > 0 }?.let { selection ->
                    val (startIndex, endIndex) = if (selection.end < selection.start) {
                        selection.end to selection.start
                    } else {
                        selection.start to selection.end
                    }
                    onSearchSelection(TextRange(startIndex, endIndex))
                }
            }
        } else {
            null
        }


    SelectionContainer(
        modifier = modifier,
        content = {
            content(
                Modifier
                    .appendTextContextMenuComponents {
                        separator()
                        when (contextMenuState) {
                            ContextMenuType.DEFAULT -> {
                                item(
                                    key = ContextMenuKey.HighlightKey,
                                    label = context.getString(L10nR.string.ss_action_highlight),
                                    leadingIcon = BlockKitR.drawable.ic_format_ink_highlighter,
                                ) {
                                    contextMenuState = ContextMenuType.HIGHLIGHT
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.UnderlineKey,
                                    label = context.getString(L10nR.string.ss_action_underline),
                                    leadingIcon = BlockKitR.drawable.ic_format_underlined,
                                ) {
                                    contextMenuState = ContextMenuType.UNDERLINE
                                }
                                separator()
                                if (canSearchSelection) {
                                    item(
                                        key = ContextMenuKey.SearchKey,
                                        label = context.getString(L10nR.string.ss_search),
                                        leadingIcon = BlockKitR.drawable.ic_search,
                                    ) {
                                        onSearch?.invoke()
                                        close()
                                    }
                                    separator()
                                }
                            }
                            ContextMenuType.HIGHLIGHT -> {
                                item(
                                    key = ContextMenuKey.Highlight.Remove,
                                    label = "❌",
                                ) {
                                    onRemoveHighlight()
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Highlight.Yellow,
                                    label = "🟡",
                                ) {
                                    onHighlight(HighlightColor.YELLOW)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Highlight.Green,
                                    label = "🟢",
                                    leadingIcon = BlockKitR.drawable.ic_highlight_green,
                                ) {
                                    onHighlight(HighlightColor.GREEN)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Highlight.Purple,
                                    label = "🟣",
                                ) {
                                    onHighlight(HighlightColor.PURPLE)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Highlight.Blue,
                                    label = "🔵",
                                ) {
                                    onHighlight(HighlightColor.BLUE)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Highlight.Orange,
                                    label = "🟠",
                                ) {
                                    onHighlight(HighlightColor.ORANGE)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Highlight.Brown,
                                    label = "🟤",
                                ) {
                                    onHighlight(HighlightColor.BROWN)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Highlight.Red,
                                    label = "🔴",
                                ) {
                                    onHighlight(HighlightColor.RED)
                                    close()
                                }
                            }
                            ContextMenuType.UNDERLINE -> {
                                item(
                                    key = ContextMenuKey.Underline.Remove,
                                    label = "❌",
                                ) {
                                    onRemoveUnderline()
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Underline.Yellow,
                                    label = "🟡",
                                ) {
                                    onUnderline(HighlightColor.YELLOW)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Underline.Green,
                                    label = "🟢",
                                ) {
                                    onUnderline(HighlightColor.GREEN)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Underline.Purple,
                                    label = "🟣",
                                ) {
                                    onUnderline(HighlightColor.PURPLE)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Underline.Blue,
                                    label = "🔵",
                                ) {
                                    onUnderline(HighlightColor.BLUE)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Underline.Orange,
                                    label = "🟠",
                                ) {
                                    onUnderline(HighlightColor.ORANGE)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Underline.Brown,
                                    label = "🟤",
                                ) {
                                    onUnderline(HighlightColor.BROWN)
                                    close()
                                }
                                separator()
                                item(
                                    key = ContextMenuKey.Underline.Red,
                                    label = "🔴",
                                ) {
                                    onUnderline(HighlightColor.RED)
                                    close()
                                }
                            }
                        }

                        separator()
                    }
                    .filterTextContextMenuComponents {
                        when (contextMenuState) {
                            ContextMenuType.DEFAULT -> selection.length > 0
                            ContextMenuType.HIGHLIGHT -> it.key is ContextMenuKey.Highlight
                            ContextMenuType.UNDERLINE -> it.key is ContextMenuKey.Underline
                        }
                    }
            )
        },
    )

    LaunchedEffect(selection) {
        contextMenuState = ContextMenuType.DEFAULT
    }
}

private fun Context.isBrowserInstalled(): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, "https://www.google.com".toUri())
    return intent.resolveActivity(packageManager) != null
}
