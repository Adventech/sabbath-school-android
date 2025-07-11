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

package ss.share.options

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.extensions.haptics.SsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.button.SsButtonDefaults
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.ShareFileURL
import io.adventech.blockkit.model.resource.ShareGroup
import io.adventech.blockkit.model.resource.ShareLinkURL
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState
import ss.libraries.circuit.navigation.ShareOptionsScreen

@CircuitInject(ShareOptionsScreen::class, SingletonComponent::class)
@Composable
fun ShareOptionsUi(state: ShareState, modifier: Modifier = Modifier) {
    val hapticFeedback = LocalSsHapticFeedback.current
    val context = LocalContext.current
    val buttonColors = SsButtonDefaults.colors(containerColor = state.themeColor ?: SsTheme.colors.primary)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            state.segments.forEachIndexed { index, group ->
                SegmentedButton(
                    selected = state.selectedGroup.title == group,
                    onClick = {
                        hapticFeedback.performSegmentSwitch()
                        state.eventSink(Event.OnSegmentSelected(group))
                    },
                    colors = SegmentedButtonDefaults.colors(),
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = state.segments.size)
                ) {
                    Text(
                        text = group,
                        style = SsTheme.typography.titleMedium
                    )
                }
            }
        }

        AnimatedContent(state.selectedGroup) { group ->
            when (group) {
                is ShareGroup.File -> FilesContent(group.files, hapticFeedback) {
                    state.eventSink(Event.OnShareFileClicked(it))
                }

                is ShareGroup.Link -> LinksContent(group.links, hapticFeedback) {
                    state.eventSink(Event.OnShareUrlSelected(it))
                }

                is ShareGroup.Unknown -> Unit
            }
        }

        Spacer(Modifier.height(8.dp))

        ElevatedButton(
            onClick = {
                hapticFeedback.performClick()
                state.eventSink(Event.OnShareButtonClicked(context))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = state.shareButtonState == ShareButtonState.ENABLED,
            colors = buttonColors,
        ) {
            if (state.shareButtonState == ShareButtonState.LOADING) {
                CircularProgressIndicator(Modifier.size(32.dp))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconBox(
                        icon = Icons.Share,
                        contentColor = Color.White
                    )
                    Text(
                        text = "Share",
                        style = SsTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun LinksContent(
    links: List<ShareLinkURL>,
    hapticFeedback: SsHapticFeedback,
    onLink: (ShareLinkURL) -> Unit,
) {
    var selectedLink by remember { mutableStateOf(links.firstOrNull()) }
    var showMenu by remember { mutableStateOf(false) }
    val cascadeState = rememberCascadeState()
    val screenWidth = (LocalWindowInfo.current.containerSize.width / LocalDensity.current.density).dp

    selectedLink?.let { link ->
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {

            OutlinedButton(
                onClick = {
                    hapticFeedback.performClick()
                    showMenu = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SsTheme.colors.dividers),
                enabled = links.size > 1
            ) {
                Text(
                    text = link.src,
                    style = SsTheme.typography.bodyLarge.copy(
                        color = SsTheme.colors.primaryForeground
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (links.size > 1) {
                    IconBox(
                        icon = Icons.ArrowDropDown,
                        contentColor = SsTheme.colors.primaryForeground
                    )
                }
            }
            CascadeDropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier,
                state = cascadeState,
                offset = DpOffset(screenWidth / 4, 0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                links.forEach { link ->
                    DropdownMenuItem(
                        text = { Text(link.title?.takeUnless { it.isEmpty() } ?: link.src) },
                        onClick = {
                            hapticFeedback.performClick()
                            selectedLink = link
                            showMenu = false
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedLink) {
        selectedLink?.let { onLink(it) }
    }
}

@Composable
private fun FilesContent(files: List<ShareFileURL>, hapticFeedback: SsHapticFeedback, onFile: (ShareFileURL) -> Unit) {
    var selectedFile by remember { mutableStateOf(files.firstOrNull()) }
    var showMenu by remember { mutableStateOf(false) }
    val cascadeState = rememberCascadeState()
    val screenWidth = (LocalWindowInfo.current.containerSize.width / LocalDensity.current.density).dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        selectedFile?.takeUnless { files.size <= 1 || it.title.isNullOrEmpty() }?.let { file ->
            TextButton(
                onClick = {
                    hapticFeedback.performClick()
                    showMenu = true
                },
                enabled = files.size > 1,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = file.title ?: file.fileName ?: "",
                        style = SsTheme.typography.titleMedium.copy(
                            color = SsTheme.colors.primaryForeground
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    if (files.size > 1) {
                        IconBox(
                            icon = Icons.ArrowDropDown,
                            contentColor = SsTheme.colors.primaryForeground
                        )
                    }
                }
            }
        }

        CascadeDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier,
            state = cascadeState,
            offset = DpOffset(screenWidth / 4, 0.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            files.forEach { file ->
                DropdownMenuItem(
                    text = { Text(file.title ?: file.fileName ?: "") },
                    onClick = {
                        hapticFeedback.performClick()
                        selectedFile = file
                        showMenu = false
                    }
                )
            }
        }

    }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { onFile(it) }
    }

}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            ShareOptionsUi(
                state = ShareState(
                    segments = listOf("Link", "File"),
                    selectedGroup = ShareGroup.Link(
                        title = "Link",
                        links = listOf(
                            ShareLinkURL(
                                src = "https://example.com/resource/hdhdud/dhdhd",
                                title = "Example Resource",
                            ),
                            ShareLinkURL(
                                src = "https://example.com/resource",
                                title = "Example Resource",
                            )
                        ),
                        selected = null,
                    ),
                    shareButtonState = ShareButtonState.ENABLED,
                    themeColor = null,
                ) {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewFile() {
    SsTheme {
        Surface {
            ShareOptionsUi(
                state = ShareState(
                    segments = listOf("Link", "File"),
                    selectedGroup = ShareGroup.File(
                        title = "File",
                        files = listOf(
                            ShareFileURL(
                                src = "https://example.com/resource/hdhdud/dhdhd",
                                title = "Example Resource",
                                fileName = null
                            ),
                            ShareFileURL(
                                src = "https://example.com/resource",
                                title = "Example Resource",
                                fileName = null
                            )
                        ),
                        selected = true
                    ),
                    shareButtonState = ShareButtonState.LOADING,
                    themeColor = null,
                ) {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
