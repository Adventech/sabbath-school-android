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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.TextStyle
import io.adventech.blockkit.model.TextStyleSize
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme

@Composable
internal fun CollapseContent(
    blockItem: BlockItem.Collapse,
    modifier: Modifier = Modifier,
    userInputState: UserInputState? = null,
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
) {
    var expanded by remember { mutableStateOf(false) }
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "card-rotation"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (expanded) 6.dp else 1.dp,
        label = "card-elevation"
    )
    val captionTextStyle = Styler.textStyle(blockStyle = captionBlockTextStyle)

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = Styler.roundedShape(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = cardElevation,
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Styler.genericBackgroundColorForInteractiveBlock()
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = blockItem.caption,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    style = captionTextStyle,
                    color = Styler.genericForegroundColorForInteractiveBlock(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = iconRotation
                    },
                    tint = captionTextStyle.color
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(contentBackgroundColor())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    blockItem.items.forEach { item ->
                        BlockContent(
                            blockItem = item,
                            nested = true,
                            userInputState = userInputState,
                            onHandleUri = onHandleUri,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun contentBackgroundColor(): Color {
    return (if (isSystemInDarkTheme()) Color(0xFF636366) else Color(0xFF8E8E93)).copy(alpha = 0.1f)
}

private val captionBlockTextStyle = TextStyle(
    typeface = null,
    color = null,
    size = TextStyleSize.BASE,
    align = null,
    offset = null
)

@PreviewLightDark
@Composable
private fun Preview() {
    BlocksPreviewTheme {
        Surface {
            CollapseContent(
                blockItem = BlockItem.Collapse(
                    id = "1",
                    style = null,
                    data = null,
                    nested = null,
                    caption = "Caption",
                    items = listOf(
                        BlockItem.Paragraph(
                            id = "2",
                            style = null,
                            data = null,
                            nested = null,
                            markdown = "This _is a paragraph_"
                        ),
                        BlockItem.Paragraph(
                            id = "3",
                            style = null,
                            data = null,
                            nested = null,
                            markdown = "This is another **paragraph**"
                        )
                    )
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
