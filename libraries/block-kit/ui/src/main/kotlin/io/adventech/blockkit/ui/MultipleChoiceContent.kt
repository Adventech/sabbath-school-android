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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import io.adventech.blockkit.ui.color.Gray300
import io.adventech.blockkit.ui.color.Gray500
import io.adventech.blockkit.ui.color.Gray800
import io.adventech.blockkit.ui.color.Sepia400
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.input.find
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme

@Composable
fun MultipleChoiceContent(
    blockItem: BlockItem.MultipleChoice,
    modifier: Modifier = Modifier,
    userInputState: UserInputState? = null,
    onHandleUri: (String) -> Unit = {},
) {
    val userInput by remember(userInputState) {
        mutableStateOf<UserInput.Checklist?>(userInputState?.find(blockItem.id))
    }

    val checkedListMap = remember(userInput) {
        blockItem.items.associate {
            it.index to (it.index in (userInput?.checked ?: emptyList()))
        }.toMutableMap()
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        blockItem.items.forEach { item ->
            MultipleChoiceItemContent(
                blockItem = item,
                modifier = Modifier,
                checked = checkedListMap[item.index] == true,
                answer = blockItem.answer,
                onCheckedChange = { index, checked ->
                    // Only one item can be checked at a time
                    checkedListMap.keys.forEach { checkedListMap[it] = false }
                    checkedListMap[index] = checked

                    userInputState?.eventSink(
                        UserInputState.Event.InputChanged(
                            UserInputRequest.Checklist(
                                blockId = blockItem.id,
                                checked = listOf(index)
                            )
                        )
                    )
                },
                onHandleUri = onHandleUri,
            )
        }
    }
}

@Composable
fun MultipleChoiceItemContent(
    blockItem: BlockItem.MultipleChoiceItem,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    answer: Int = -1,
    onCheckedChange: (Int, Boolean) -> Unit = { _, _ -> },
    onHandleUri: (String) -> Unit = {},
) {
    var isChecked by remember(checked) { mutableStateOf(checked) }
    val style = Styler.textStyle(blockItem.style?.text)

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .border(
                width = 1.dp,
                color = Styler.borderColor(),
                shape = Styler.roundedShape()
            )
            .clip(Styler.roundedShape())
            .clickable {
                isChecked = !isChecked
                onCheckedChange(blockItem.index, isChecked)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val buttonContainerColor by animateColorAsState(
            if (isChecked) Styler.borderColor() else checkmarkBackgroundColor()
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(buttonContainerColor),
            contentAlignment = Alignment.Center,
        ) {

            IconButton(
                modifier = Modifier,
                onClick = {
                    // Only one item can be checked at a time
                    if (!isChecked) {
                        isChecked = true
                        onCheckedChange(blockItem.index, true)
                    }
                },
            ) {
                Box(
                    modifier = Modifier.background(checkMarkBackgroundColor(isChecked), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecked) {
                        if (blockItem.index == answer) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(IconSize)
                                    .padding(2.dp),
                                tint = checkMarkColor()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(IconSize)
                                    .padding(2.dp),
                                tint = checkMarkColor()
                            )
                        }
                    } else {
                        val circleColor = Styler.borderColor()
                        Canvas(
                            Modifier
                                .wrapContentSize(Alignment.Center)
                        ) {
                            // Draw the circle
                            val strokeWidth = RadioStrokeWidth.toPx()
                            drawCircle(
                                circleColor,
                                radius = (IconSize / 2).toPx() - strokeWidth / 2,
                                style = Stroke(strokeWidth)
                            )
                        }

                    }
                }
            }
        }

        Spacer(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Styler.borderColor())
        )

        MarkdownText(
            markdownText = blockItem.markdown,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = Styler.textColor(blockItem.style?.text),
            style = style,
            onHandleUri = onHandleUri,
        )
    }
}

@Composable
private fun checkMarkBackgroundColor(selected: Boolean): Color {
    val light = if (selected) Color.White else Color.Gray300
    val sepia = if (selected) Color.White else Color.Sepia400
    val dark = if (selected) Color.White else Color.Gray800

    return Styler.themedColor(
        light = light,
        sepia = sepia,
        dark = dark
    )
}

@Composable
private fun checkMarkColor(): Color {
    val light = Color.Gray500
    val sepia = Color.Sepia400
    val dark = Styler.borderColor()

    return Styler.themedColor(light, sepia, dark)
}

private val RadioStrokeWidth = 2.dp
private val IconSize = 24.dp

private val previewBlockItem = BlockItem.MultipleChoice(
    id = "",
    style = null,
    data = null,
    nested = null,
    ordered = true,
    start = 0,
    items = listOf(
        BlockItem.MultipleChoiceItem(
            id = "0",
            style = null,
            data = null,
            nested = null,
            index = 0,
            markdown = "Yes, I agree."
        ),
        BlockItem.MultipleChoiceItem(
            id = "1",
            style = null,
            data = null,
            nested = null,
            index = 1,
            markdown = "I have questions."
        )
    ),
    answer = 0
)

@PreviewLightDark
@Composable
private fun Preview() {
    BlocksPreviewTheme {
        Surface(contentColor = LocalReaderStyle.current.theme.background()) {
            MultipleChoiceContent(
                blockItem = previewBlockItem,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSepia() {
    BlocksPreviewTheme(theme = ReaderStyleConfig(theme = ReaderStyle.Theme.Sepia)) {
        Surface(contentColor = LocalReaderStyle.current.theme.background()) {
            MultipleChoiceContent(
                blockItem = previewBlockItem,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
