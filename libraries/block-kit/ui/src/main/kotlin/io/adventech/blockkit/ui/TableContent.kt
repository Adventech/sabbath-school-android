/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Constraints
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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

    val allRows = remember(blockItem) {
        val rows = mutableListOf<List<TableCell>>()
        if (blockItem.header.isNotEmpty()) {
            rows.add(blockItem.header)
        }
        blockItem.rows.forEach { row ->
            rows.add(row.items)
        }
        rows
    }

    val columnCount = remember(allRows) { allRows.maxOfOrNull { it.size } ?: 0 }
    val rowCount = allRows.size

    if (columnCount == 0 || rowCount == 0) return

    val (screenWidth, screenHeight) = LocalConfiguration.current.run {
        screenWidthDp to screenHeightDp
    }
    val widthSizeClass = WindowSizeClass.calculateFromSize(DpSize(screenWidth.dp, screenHeight.dp)).widthSizeClass

    val density = LocalDensity.current
    val cellWidthPx = remember(widthSizeClass, columnCount, screenWidth, density) {
        val widthDp = when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> screenWidth * 0.67
            WindowWidthSizeClass.Medium -> screenWidth * 0.5
            WindowWidthSizeClass.Expanded -> if (columnCount > 0) (screenWidth / columnCount).toDouble() else screenWidth * 0.67
            else -> screenWidth * 0.67
        }
        with(density) { widthDp.dp.roundToPx() }
    }

    val borderColor = Styler.borderColor()
    val headerColor = Styler.genericBackgroundColorForInteractiveBlock()
    val borderWidthPx = with(density) { BorderWidth.toPx() }
    val hasHeader = blockItem.header.isNotEmpty()

    val layoutInfo = remember { TableLayoutInfo() }

    Box(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(Styler.padding(blockPaddingStyle ?: defaultWrapperPaddingStyle))
            .border(BorderWidth, borderColor)
    ) {
        Layout(
            modifier = Modifier.drawBehind {
                // Draw background for header row
                if (hasHeader && layoutInfo.rowHeights.isNotEmpty()) {
                    val headerHeight = layoutInfo.rowHeights[0].toFloat()
                    drawRect(
                        color = headerColor,
                        topLeft = Offset.Zero,
                        size = Size(size.width, headerHeight)
                    )
                }

                // Draw vertical dividers
                var divX = 0f
                for (c in 0 until layoutInfo.columnWidths.size - 1) {
                    divX += layoutInfo.columnWidths[c]
                    drawLine(
                        color = borderColor,
                        start = Offset(divX, 0f),
                        end = Offset(divX, size.height),
                        strokeWidth = borderWidthPx
                    )
                }
            },
            content = {
                allRows.forEach { rowCells ->
                    for (c in 0 until columnCount) {
                        val cell = rowCells.getOrNull(c)
                        if (cell != null) {
                            Box(modifier = Modifier, contentAlignment = Alignment.CenterStart) {
                                Column {
                                    cell.items.forEach { item ->
                                        BlockContent(
                                            blockItem = item,
                                            modifier = Modifier.padding(12.dp),
                                            parent = blockItem,
                                            userInputState = userInputState,
                                            onHandleUri = onHandleUri,
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier)
                        }
                    }
                }
            }
        ) { measurables, _ ->
            val columnWidths = IntArray(columnCount)
            val rowHeights = IntArray(rowCount)

            val placeables = measurables.mapIndexed { index, measurable ->
                val c = index % columnCount
                val r = index / columnCount

                val placeable = measurable.measure(Constraints(maxWidth = cellWidthPx))
                if (placeable.width > columnWidths[c]) {
                    columnWidths[c] = placeable.width
                }
                if (placeable.height > rowHeights[r]) {
                    rowHeights[r] = placeable.height
                }
                placeable
            }

            layoutInfo.columnWidths = columnWidths
            layoutInfo.rowHeights = rowHeights

            val totalWidth = columnWidths.sum()
            val totalHeight = rowHeights.sum()

            layout(totalWidth, totalHeight) {
                var y = 0
                for (r in 0 until rowCount) {
                    var x = 0
                    for (c in 0 until columnCount) {
                        val placeable = placeables[r * columnCount + c]
                        val yOffset = y + (rowHeights[r] - placeable.height) / 2
                        placeable.placeRelative(x, yOffset)

                        x += columnWidths[c]
                    }
                    y += rowHeights[r]
                }
            }
        }
    }
}

private val BorderWidth = 0.4.dp

private class TableLayoutInfo {
    var columnWidths: IntArray = intArrayOf()
    var rowHeights: IntArray = intArrayOf()
}

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
