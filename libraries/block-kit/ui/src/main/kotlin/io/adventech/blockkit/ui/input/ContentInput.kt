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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import io.adventech.blockkit.model.input.Underline
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.ui.color.toColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Remembers and returns an immutable list of highlights associated with the given [blockId].
 *
 * This function extracts highlights from the [userInputState], filtering only inputs
 * of type [UserInput.Highlights] that match the specified [blockId].
 *
 * @param blockId The identifier of the block to filter highlights for.
 * @param userInputState The state containing user inputs, which may include highlights.
 * @return An immutable list of highlights corresponding to the [blockId].
 */
@Composable
internal fun rememberContentHighlights(
    blockId: String,
    userInputState: UserInputState?,
) = remember(blockId, userInputState) {
    userInputState?.find<UserInput.Highlights>(blockId)
        ?.highlights
        ?.toImmutableList() ?: persistentListOf()
}

/**
 * Remembers and returns an immutable list of underlines associated with the given [blockId].
 *
 * This function extracts underlines from the [userInputState], filtering only inputs
 * of type [UserInput.Underlines] that match the specified [blockId].
 *
 * @param blockId The identifier of the block to filter underlines for.
 * @param userInputState The state containing user inputs, which may include underlines.
 * @return An immutable list of underlines corresponding to the [blockId].
 */
@Composable
internal fun rememberContentUnderlines(
    blockId: String,
    userInputState: UserInputState?,
) = remember(blockId, userInputState) {
    userInputState?.find<UserInput.Underlines>(blockId)
        ?.underlines
        ?.toImmutableList() ?: persistentListOf()
}

internal fun Modifier.drawUnderlines(
    text: AnnotatedString,
    layoutResult: TextLayoutResult?,
    underlines: ImmutableList<Underline>,
): Modifier = this.
    drawBehind {
        layoutResult?.let { result ->
            underlines.forEach { underline ->
                if (underline.startIndex >= 0 && underline.endIndex <= text.length) {
                    val startRect = result.getBoundingBox(underline.startIndex)
                    val endRect = result.getBoundingBox(underline.endIndex - 1)

                    val y = endRect.bottom + 2f // slight offset for better visual separation

                    drawLine(
                        color = underline.color.toColor(),
                        start = Offset(startRect.left, y),
                        end = Offset(endRect.right, y),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
