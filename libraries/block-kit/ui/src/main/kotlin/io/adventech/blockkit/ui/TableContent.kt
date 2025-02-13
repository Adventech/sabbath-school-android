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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.TableCell
import io.adventech.blockkit.model.TableRow
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.LocalBlocksStyle
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TableContent(
    blockItem: BlockItem.TableBlock,
    modifier: Modifier = Modifier,
    userInputState: UserInputState? = null,
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> }
) {
    val blocksStyle = LocalBlocksStyle.current
    val blockStyle = blockItem.style?.block
    val blockPaddingStyle = blockStyle?.padding

    val defaultWrapperPaddingStyle = blocksStyle?.inline?.all?.wrapper?.padding?.takeUnless {
        blockPaddingStyle != null || blockItem.nested == true
    }

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(Styler.padding(blockPaddingStyle ?: defaultWrapperPaddingStyle))
            .border(BorderWidth, Styler.borderColor()),
    ) {

        Column(modifier = Modifier) {

            TableRowContent(
                cells = blockItem.header,
                parent = blockItem,
                userInputState = userInputState,
                onHandleUri = onHandleUri,
                color = Styler.genericBackgroundColorForInteractiveBlock(),
            )

            blockItem.rows.forEach { row ->
                TableRowContent(
                    cells = row.items,
                    parent = blockItem,
                    userInputState = userInputState,
                    onHandleUri = onHandleUri,
                )
            }
        }
    }

}

@Composable
private fun TableRowContent(
    cells: List<TableCell>,
    parent: BlockItem,
    userInputState: UserInputState?,
    onHandleUri: (String, BlockData?) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Transparent
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cells.forEachIndexed { index, cell ->
            TableCell(
                cell = cell,
                rowCount = cells.size,
                parent = parent,
                userInputState = userInputState,
                onHandleUri = onHandleUri,
                modifier = Modifier.background(color),
            )

            if (index < cells.size - 1) {
                VerticalDivider(
                    modifier = Modifier,
                    thickness = BorderWidth,
                    color = Styler.borderColor()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun TableCell(
    cell: TableCell,
    rowCount: Int,
    parent: BlockItem,
    userInputState: UserInputState?,
    onHandleUri: (String, BlockData?) -> Unit,
    modifier: Modifier = Modifier
) {
    val (screenWidth, screenHeight) = LocalConfiguration.current.run {
        screenWidthDp to screenHeightDp
    }

    val widthSizeClass = WindowSizeClass.calculateFromSize(DpSize(screenWidth.dp, screenHeight.dp)).widthSizeClass

    val cellWidth = remember(widthSizeClass) {
        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> screenWidth * 0.67
            WindowWidthSizeClass.Medium -> screenWidth * 0.5
            WindowWidthSizeClass.Expanded -> (screenWidth / rowCount).toDouble()
            else -> screenWidth * 0.67
        }
    }

    Column(
        modifier = modifier.widthIn(max = cellWidth.dp)
    ) {
        cell.items.forEach { item ->
            BlockContent(
                blockItem = item,
                modifier = Modifier
                    .padding(12.dp),
                parent = parent,
                userInputState = userInputState,
                onHandleUri = onHandleUri,
            )

            // Horizontal divider
        }
    }
}

private val BorderWidth = 0.4.dp


@PreviewScreenSizes
@Composable
private fun Preview() {
    BlocksPreviewTheme {
        Surface {
            TableContent(tableBlock, Modifier.padding(16.dp))
        }
    }
}

private val tableBlock = BlockItem.TableBlock(
    id = "",
    style = null,
    data = null,
    nested = null,
    rows = listOf(
        TableRow(
            items = listOf(
                TableCell(
                    items = listOf(
                        paragraph("1.Wild Beasts in Babylon Isa 13:21"),
                        paragraph("2.Ruled the nations Isa 14:6"),
                        paragraph("3.Attached Israel Jer 51:49"),
                    )
                ),
                TableCell(
                    items = listOf(
                        paragraph("1.This is Great *Babylon* Dan 4:30"),
                        paragraph("2.Habitation of devils Rev 18:2"),
                        paragraph("3.Attached God's people Rev 17:6, 18:24"),
                    )
                ),
                TableCell(
                    items = listOf(
                        paragraph("1.This is Great *Babylon* Dan 4:30"),
                        paragraph("2.Habitation of devils Rev 18:2"),
                        paragraph("3.Attached God's people Rev 17:6, 18:24"),
                    )
                ),
            )
        )
    ),
    header = listOf(
        TableCell(
            items = listOf(
                paragraph("Ancient Babylon"),
            )
        ),
        TableCell(
            items = listOf(
                paragraph("Modern Babylon"),
            )
        ),
        TableCell(
            items = listOf(
                paragraph("End-time Babylon"),
            )
        )
    ),
)

private fun paragraph(markdown: String) = BlockItem.Paragraph(
    id = "id",
    style = null,
    data = null,
    nested = null,
    markdown = markdown
)
