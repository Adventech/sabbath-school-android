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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.secondaryBackground
import io.adventech.blockkit.ui.style.secondaryForeground
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme

@Composable
internal fun AppealContent(
    blockItem: BlockItem.Appeal,
    modifier: Modifier = Modifier,
    userInputState: UserInputState? = null,
    onHandleUri: (String) -> Unit = {},
) {
    val theme = LocalReaderStyle.current.theme
    val containerColor = theme.secondaryBackground()
    val textColor = theme.secondaryForeground()
    val userInput = rememberUserInput(blockItem.id, userInputState)
    var isChecked by remember(userInput) { mutableStateOf(userInput.appeal) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = textColor,
        ),
        shape = Styler.roundedShape(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MarkdownText(
                markdownText = blockItem.markdown,
                modifier = Modifier.fillMaxWidth(),
                color = textColor,
                textAlign = TextAlign.Center,
                onHandleUri = onHandleUri,
            )

            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    userInputState?.eventSink(
                        UserInputState.Event.InputChanged(
                            UserInputRequest.Appeal(
                                blockId = blockItem.id,
                                appeal = isChecked,
                            )
                        )
                    )
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = textColor,
                    uncheckedColor = textColor,
                    checkmarkColor = theme.background(),
                ),
            )
        }
    }

}

@Composable
private fun rememberUserInput(
    blockId: String,
    userInputState: UserInputState?,
) = remember(blockId, userInputState) {
    userInputState?.input?.firstOrNull {
        it.blockId == blockId && it is UserInput.Appeal
    } as? UserInput.Appeal ?: UserInput.Appeal(
        blockId = blockId,
        id = "",
        timestamp = 0,
        appeal = false,
    )
}

@PreviewLightDark
@Composable
private fun Preview() {
    BlocksPreviewTheme {
        Surface {
            AppealContent(
                blockItem = BlockItem.Appeal(
                    id = "1",
                    style = null,
                    markdown = "This is an appeal",
                    data = null,
                    nested = false
                )
            )
        }
    }
}

