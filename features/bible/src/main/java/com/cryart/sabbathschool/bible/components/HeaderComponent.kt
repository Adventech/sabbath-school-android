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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import app.ss.design.compose.theme.LabelMedium
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.models.SSBibleVerses
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
            SsTheme {
                val displayOptions by displayOptionsFlow.collectAsState(initial = SSReadingDisplayOptions(isSystemInDarkTheme()))
                val bibleVerses by bibleVersesFlow.collectAsState(initial = emptyList())
                val backgroundColor = Color.parse(displayOptions.themeColor(LocalContext.current))
                val contentColor = contentColor(displayOptions.displayTheme(LocalContext.current))

                Surface(
                    color = backgroundColor,
                    contentColor = contentColor
                ) {
                    HeaderRow(
                        spec = HeaderRowSpec(
                            bibleVerses = bibleVerses,
                            lastBibleUsed = lastBibleUsed ?: "",
                            callbacks = callbacks,
                        )
                    )
                }
            }
        }
    }
}

@Composable
@Stable
private fun contentColor(theme: String): Color {
    return when (theme) {
        SSReadingDisplayOptions.SS_THEME_DARK -> Color.White
        else -> Color.Black
    }
}

@Composable
fun HeaderRow(
    spec: HeaderRowSpec,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {

        IconButton(
            onClick = {
                spec.callbacks.onClose()
            }
        ) {
            IconBox(
                icon = Icons.Close,
                contentColor = contentColor,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        BibleVersionsMenu(spec = spec)
    }
}

@Composable
private fun BibleVersionsMenu(
    spec: HeaderRowSpec,
    contentColor: Color = LocalContentColor.current
) {
    val (bibleVerses, lastBibleUsed, callbacks) = spec
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(lastBibleUsed) }
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.CenterEnd)
    ) {

        TextButton(onClick = { expanded = true }) {
            Text(
                selected,
                style = LabelMedium
            )
            IconBox(
                Icons.ArrowDropDown,
                modifier = Modifier.rotate(iconRotation),
                contentColor = contentColor,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            bibleVerses.forEach { verse ->
                DropdownMenuItem(
                    text = {
                        Text(
                            verse.name,
                            style = LabelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        val version = verse.name
                        selected = version
                        expanded = false
                        callbacks.versionSelected(version)
                    },
                    modifier = Modifier
                        .testTag("DropdownMenuItem-${verse.name}"),
                    trailingIcon = {
                        if (verse.name == selected) {
                            IconBox(Icons.Check)
                        }
                    },
                )
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
    SsTheme {
        HeaderRow(
            spec = HeaderRowSpec(
                bibleVerses = emptyList(),
                lastBibleUsed = "KJV",
                callbacks = object : HeaderComponent.Callbacks {
                    override fun onClose() {
                    }

                    override fun versionSelected(version: String) {
                    }
                },
            ),
            modifier = Modifier
                .height(50.dp)
                .padding(6.dp),
        )
    }
}
