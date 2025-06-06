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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.TextStyle
import io.adventech.blockkit.model.TextStyleSize
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import io.adventech.blockkit.ui.color.Sepia100
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.input.find
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyle
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksDynamicPreviewTheme

@Composable
internal fun QuestionContent(
    blockItem: BlockItem.Question,
    modifier: Modifier = Modifier,
    inputState: UserInputState? = null,
    onHandleUri: (String) -> Unit = {},
) {
    val input by remember(inputState) {
        mutableStateOf<UserInput.Question?>(inputState?.find(blockItem.id))
    }

    var currentSelection by remember { mutableStateOf(TextRange.Zero) }
    var textFieldValue by rememberSaveable(input, stateSaver = TextFieldValue.Saver) {
        val content = input?.answer ?: ""
        mutableStateOf(TextFieldValue(text = content, selection = currentSelection))
    }
    // Use a separate state to store the latest value for disposal
    var latestTextFieldValue by remember { mutableStateOf(textFieldValue) }
    val inputTextStyle = Styler.textStyle(blockStyle = inputBlockTextStyle)

    var inputLines by remember { mutableIntStateOf(INPUT_MIN_LINES) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = answerBackgroundColor(), contentColor = inputTextStyle.color
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (blockItem.markdown.isNotEmpty()) {
                QuestionText(
                    blockItem = blockItem,
                    userInputState = inputState,
                    onHandleUri = onHandleUri,
                    modifier = Modifier
                        .background(Styler.genericBackgroundColorForInteractiveBlock())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            InputBox(lines = inputLines, modifier = Modifier.fillMaxWidth()) { contentModifier ->
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        currentSelection = it.selection
                    },
                    modifier = contentModifier
                        .padding(answerPaddingValues()),
                    minLines = INPUT_MIN_LINES,
                    maxLines = INPUT_MAX_LINES,
                    textStyle = inputTextStyle,
                    cursorBrush = SolidColor(inputTextStyle.color),
                    onTextLayout = { layoutResult ->
                        inputLines = layoutResult.lineCount.coerceIn(INPUT_MIN_LINES, INPUT_MAX_LINES)
                    }
                )
            }
        }
    }

    // Update the latest value whenever textFieldValue changes
    LaunchedEffect(textFieldValue) { latestTextFieldValue = textFieldValue }
    DisposableEffect(Unit) {
        // Save the latest input value when the composable is disposed
        onDispose {
            val latestInput = UserInputRequest.Question(
                blockId = blockItem.id,
                answer = latestTextFieldValue.text.trim(),
            )
            if (latestInput.answer != input?.answer) {
                inputState?.eventSink?.invoke(UserInputState.Event.InputChanged(latestInput))
            }
        }
    }
}

@Composable
private fun QuestionText(
    blockItem: BlockItem.Question,
    userInputState: UserInputState?,
    onHandleUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val paragraphBlock = remember(blockItem) {
        BlockItem.Paragraph(
            id = blockItem.id,
            style = blockItem.style,
            data = blockItem.data,
            nested = null,
            markdown = blockItem.markdown,
        )
    }

    ParagraphContent(
        blockItem = paragraphBlock,
        modifier = modifier,
        parent = blockItem,
        inputState = userInputState,
        onHandleUri = onHandleUri,
    )
}

@Composable
private fun InputBox(
    lines: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val verticalColor = Color.Red.copy(alpha = 0.3f)
    val horizontalColor = DividerDefaults.color.copy(alpha = 0.5f)
    Box(
        modifier = modifier.height(IntrinsicSize.Min),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        ) {
            val height = size.height
            val width = size.width

            val xOffset = VERTICAL_LINE_OFFSET_PX // offset pixels from the left
            // Draw vertical line
            drawLine(
                color = verticalColor,
                start = Offset(xOffset, 0f),
                end = Offset(xOffset, size.height),
                strokeWidth = 0.5.dp.toPx(),
            )

            // Draw horizontal lines dynamically based on the `lines` parameter
            for (i in 1 until lines) {
                val yOffset = i * (height / lines)
                drawLine(
                    color = horizontalColor,
                    start = Offset(0f, yOffset),
                    end = Offset(width, yOffset),
                    strokeWidth = 0.5.dp.toPx(),
                )
            }
        }

        content(
            Modifier
                .fillMaxSize()
                .zIndex(3f)
        )
    }
}

@Composable
private fun answerBackgroundColor(): Color {
    val light = Color(0xFFfaf8fa)
    val serpia = Color.Sepia100
    val dark = Color(0xFF111827)

    return when (LocalReaderStyle.current.theme) {
        ReaderStyle.Theme.Light -> light
        ReaderStyle.Theme.Dark -> dark
        ReaderStyle.Theme.Auto -> if (isSystemInDarkTheme()) dark else light
        ReaderStyle.Theme.Sepia -> serpia
    }
}

@Composable
private fun answerPaddingValues(): PaddingValues {
    val density = LocalDensity.current
    val startPadding = with(density) {
        VERTICAL_LINE_OFFSET_PX.toDp()
    } + 8.dp

    return PaddingValues(
        start = startPadding,
        end = 8.dp,
        top = 0.dp,
        bottom = 0.dp
    )
}

private const val INPUT_MIN_LINES = 3
private const val INPUT_MAX_LINES = 15
private const val VERTICAL_LINE_OFFSET_PX = 100f

private val inputBlockTextStyle = TextStyle(
    typeface = null,
    color = null,
    size = TextStyleSize.BASE,
    align = null,
    offset = null,
)

@PreviewLightDark
@Composable
internal fun QuestionContentPreview() {
    BlocksDynamicPreviewTheme {
        QuestionContent(
            blockItem = BlockItem.Question(
                id = "1",
                style = null,
                data = null,
                nested = null,
                markdown = "What is the meaning of life?",
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
