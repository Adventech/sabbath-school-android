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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.Styler

@Composable
internal fun BlockListContent(
    blockItem: BlockItem.BlockList,
    modifier: Modifier = Modifier,
    userInputState: UserInputState? = null,
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blockItem.items.forEach { item ->
            BlockContent(
                blockItem = item,
                nested = true,
                parent = blockItem,
                userInputState = userInputState,
                onHandleUri = onHandleUri,
            )
        }
    }
}

@Composable
internal fun BlockListItemContent(
    blockItem: BlockItem.BlockListItem,
    modifier: Modifier = Modifier,
    bullet: String = "",
    userInputState: UserInputState? = null,
    onHandleUri: (String) -> Unit = {},
) {
    val blockStyle = blockItem.style?.text
    val color = Styler.textColor(blockStyle)
    val style = Styler.textStyle(blockStyle)

    val paragraphBlock = remember(blockItem) {
        BlockItem.Paragraph(
            id = blockItem.id,
            style = blockItem.style,
            data = blockItem.data,
            nested = blockItem.nested,
            markdown = blockItem.markdown,
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = bullet,
            color = color,
            style = style,
        )

        ParagraphContent(
            blockItem = paragraphBlock,
            modifier = Modifier.weight(1f),
            parent = blockItem,
            inputState = userInputState,
            onHandleUri = onHandleUri,
        )
    }
}
