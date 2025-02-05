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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.BlockStyleTemplate

@Composable
internal fun QuoteContent(
    blockItem: BlockItem.Quote,
    modifier: Modifier = Modifier,
    inputState: UserInputState? = null,
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
) {
    val style = BlockStyleTemplate.DEFAULT
    val color = style.textColorDefault()
    Column(
        modifier = modifier
            .drawBehind {
                drawLine(
                    color = color,
                    strokeWidth = 10f,
                    start = Offset(12.dp.value, 0f),
                    end = Offset(12.dp.value, size.height),
                    cap = StrokeCap.Round
                )
            }
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blockItem.items.forEach {
            BlockContent(blockItem = it, nested = true, userInputState = inputState, onHandleUri = onHandleUri)
        }
    }
}
