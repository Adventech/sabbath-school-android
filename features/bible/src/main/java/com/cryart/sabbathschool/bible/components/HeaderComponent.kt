/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.bible.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.ss.models.SSBibleVerses
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.LabelMedium
import com.cryart.design.theme.SSTheme
import com.cryart.design.theme.parse
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.core.model.displayTheme
import com.cryart.sabbathschool.core.model.themeColor
import kotlinx.coroutines.flow.Flow

class HeaderComponent(
    composeView: ComposeView,
    private val displayOptionsFlow: Flow<SSReadingDisplayOptions>,
    private val bibleVersesFlow: Flow<List<SSBibleVerses>>,
    private val lastBibleUsed: String?,
    private val callbacks: Callbacks
) {

    interface Callbacks {
        fun onClose()
        fun versionSelected(version: String)
    }

    init {
        composeView.setContent {
            SSTheme {
                val displayOptions = displayOptionsFlow.collectAsState(
                    initial = SSReadingDisplayOptions(isSystemInDarkTheme())
                ).value
                val bibleVerses = bibleVersesFlow.collectAsState(
                    initial = emptyList()
                ).value

                val backgroundColor = Color.parse(displayOptions.themeColor(LocalContext.current))
                Surface(
                    color = backgroundColor,
                    contentColor = displayOptions.contentColor()
                ) {
                    HeaderRow(
                        bibleVerses = bibleVerses,
                        lastBibleUsed = lastBibleUsed ?: "",
                        callbacks = callbacks,
                    )
                }
            }
        }
    }
}

@Composable
private fun SSReadingDisplayOptions.contentColor(context: Context = LocalContext.current): Color {
    return when (displayTheme(context)) {
        SSReadingDisplayOptions.SS_THEME_DARK -> Color.White
        else -> Color.Black
    }
}

@Composable
fun HeaderRow(
    bibleVerses: List<SSBibleVerses>,
    lastBibleUsed: String,
    modifier: Modifier = Modifier,
    callbacks: HeaderComponent.Callbacks? = null,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {

        IconButton(
            onClick = {
                callbacks?.onClose()
            }
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close")
        }

        Spacer(modifier = Modifier.weight(1f))

        BibleVersionsMenu(
            bibleVerses = bibleVerses,
            lastBibleUsed = lastBibleUsed,
            callbacks = callbacks,
        )
    }
}

@Composable
private fun BibleVersionsMenu(
    bibleVerses: List<SSBibleVerses>,
    lastBibleUsed: String,
    callbacks: HeaderComponent.Callbacks? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(lastBibleUsed) }
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.CenterEnd)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(Dimens.grid_2)
                .clickable { expanded = true }
                .testTag("drop-down")
        ) {
            Text(
                selected,
                style = LabelMedium
            )
            Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Arrow Drop-Down",
                modifier = Modifier.rotate(iconRotation)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            bibleVerses.forEach { verse ->
                DropdownMenuItem(
                    onClick = {
                        val version = verse.name
                        selected = version
                        expanded = false
                        callbacks?.versionSelected(version)
                    },
                    modifier = Modifier
                        .testTag("DropdownMenuItem-${verse.name}")
                ) {
                    Text(
                        verse.name,
                        style = LabelMedium,
                    )
                    if (verse.name == selected) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        )
                    }
                }
            }
        }
    }

    if (selected.isEmpty() && bibleVerses.isNotEmpty()) {
        LaunchedEffect(
            key1 = Any(),
            block = {
                selected = bibleVerses.first().name
            }
        )
    }
}

@Preview(
    name = "Header"
)
@Composable
private fun HeaderRowPreview() {
    SSTheme {
        HeaderRow(
            bibleVerses = emptyList(),
            lastBibleUsed = "KJV",
            modifier = Modifier
                .height(50.dp)
                .padding(6.dp),
            callbacks = object : HeaderComponent.Callbacks {
                override fun onClose() {
                }

                override fun versionSelected(version: String) {
                }
            },
        )
    }
}
