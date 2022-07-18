/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.bible

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.parse
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.core.model.displayTheme
import com.cryart.sabbathschool.core.model.themeColor
import kotlinx.coroutines.flow.Flow

internal class ToolbarComponent(
    composeView: ComposeView,
    private val stateFlow: Flow<ToolbarState>,
    private val callbacks: Callbacks
) {

    interface Callbacks {
        fun onClose()
        fun versionSelected(version: String)
    }

    init {
        composeView.setContent {
            SsTheme {
                val uiState by stateFlow.collectAsState(ToolbarState.Loading)

                BibleToolbar(
                    state = uiState,
                    callbacks = callbacks,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
internal fun BibleToolbar(
    state: ToolbarState,
    callbacks: ToolbarComponent.Callbacks,
    modifier: Modifier = Modifier
) {
    var displayOptions: SSReadingDisplayOptions? = null

    val spec = when (state) {
        is ToolbarState.Success -> {
            displayOptions = state.displayOptions
            ToolbarSpec(
                bibleVersions = state.bibleVersions,
                preferredBible = state.preferredBibleVersion,
                callbacks = callbacks,
            )
        }
        else -> {
            ToolbarSpec(
                bibleVersions = emptySet(),
                preferredBible = "",
                callbacks = callbacks,
            )
        }
    }

    val options = displayOptions ?: SSReadingDisplayOptions(isSystemInDarkTheme())
    val backgroundColor = Color.parse(options.themeColor(LocalContext.current))
    val contentColor = contentColor(options.displayTheme(LocalContext.current))

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
    ) {
        BibleToolbarRow(spec = spec)
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
internal fun BibleToolbarRow(
    spec: ToolbarSpec,
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

        if (spec.bibleVersions.isNotEmpty()) {
            BibleVersionsMenu(spec = spec)
        }
    }
}

@Composable
private fun BibleVersionsMenu(
    spec: ToolbarSpec,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current
) {
    val (bibleVersions, preferredBible, callbacks) = spec
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(preferredBible) }
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.CenterEnd)
    ) {

        TextButton(onClick = { expanded = true }) {
            Text(
                selected,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 16.sp
                ),
                color = contentColor
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
            bibleVersions.forEach { version ->
                DropdownMenuItem(
                    text = {
                        Text(
                            version,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        selected = version
                        expanded = false
                        callbacks.versionSelected(version)
                    },
                    modifier = Modifier,
                    trailingIcon = {
                        if (version == selected) {
                            IconBox(Icons.Check)
                        }
                    },
                )
            }
        }
    }

    if (selected.isEmpty() && bibleVersions.isNotEmpty()) {
        LaunchedEffect(
            key1 = Any(),
            block = {
                selected = bibleVersions.first()
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
        Surface {
            BibleToolbar(
                state = ToolbarState.Success(
                    bibleVersions = setOf("NKJV", "KJV"),
                    preferredBibleVersion = "KJV",
                ),
                callbacks = object : ToolbarComponent.Callbacks {
                    override fun onClose() {
                    }

                    override fun versionSelected(version: String) {
                    }
                },
                modifier = Modifier
            )
        }
    }
}
