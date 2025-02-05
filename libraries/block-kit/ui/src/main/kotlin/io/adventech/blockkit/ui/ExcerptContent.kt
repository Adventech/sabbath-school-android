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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.Styler

@Composable
internal fun ExcerptContent(
    blockItem: BlockItem.Excerpt,
    modifier: Modifier = Modifier,
    userInputState: UserInputState? = null,
) {
    var selectedOption by remember { mutableStateOf(blockItem.options.firstOrNull()) }
    val selectedItem = remember(selectedOption) { blockItem.items.firstOrNull { it.option == selectedOption } }
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopEnd),
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier,
            offset = DpOffset(-(16).dp, 0.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            ExcerptOptions(
                options = blockItem.options,
                selectedOption = selectedOption,
                onOptionSelected = { option ->
                    expanded = false
                    selectedOption = option
                }
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.End)
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = selectedOption ?: "",
                modifier = Modifier,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = Styler.defaultFontFamily()
                ),
            )

            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null
            )
        }

        selectedItem?.let {
            ExcerptItemContent(it, Modifier.fillMaxWidth(), userInputState)
        }
    }
}

@Composable
fun ExcerptItemContent(
    blockItem: BlockItem.ExcerptItem,
    modifier: Modifier = Modifier,
    userInputState: UserInputState? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        blockItem.items.forEach {
            BlockContent(it, nested = blockItem.nested, parent = blockItem, userInputState = userInputState)
        }
    }
}

@Composable
fun ExcerptOptions(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
) {
    options.forEach { option ->
        DropdownMenuItem(
            onClick = { onOptionSelected(option) },
            modifier = Modifier,
            text = {
                ListItem(
                    headlineContent = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = Styler.defaultFontFamily(),
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    },
                    trailingContent = {
                        if (selectedOption == option) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MenuDefaults.containerColor
                    )
                )
            }
        )
    }
}
