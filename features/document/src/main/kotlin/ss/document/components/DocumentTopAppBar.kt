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

package ss.document.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material.icons.rounded.TextFormat
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButtonSlot
import app.ss.design.compose.widget.icon.Icons
import io.adventech.blockkit.model.resource.Segment
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ss.document.components.segment.dateDisplay
import androidx.compose.material.icons.Icons as MaterialIcons
import app.ss.translations.R as L10nR

enum class DocumentTopAppBarAction(
    val icon: ImageVector,
    val title: Int,
    val primary: Boolean,
) {
    Audio(
        icon = MaterialIcons.Rounded.Headphones,
        title = L10nR.string.ss_media_audio,
        primary = true,
    ),
    Video(
        icon = MaterialIcons.Rounded.OndemandVideo,
        title = L10nR.string.ss_media_videos,
        primary = true,
    ),
    Pdf(
        icon = MaterialIcons.AutoMirrored.Rounded.Article,
        title = L10nR.string.ss_pdf_original,
        primary = false,
    ),
    DisplayOptions(
        icon = MaterialIcons.Rounded.TextFormat,
        title = L10nR.string.ss_settings_display_options,
        primary = false,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    collapsible: Boolean = false,
    collapsed: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: ImmutableList<DocumentTopAppBarAction> = persistentListOf(),
    onNavBack: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = modifier,
        offset = DpOffset((-16).dp, 0.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = SsTheme.colors.primaryBackground,
    ) {
        actions.filter { it.primary == false }.forEach {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(it.title),
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                onClick = {
                    expanded = false
                },
                modifier = Modifier,
                leadingIcon = {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = stringResource(it.title),
                        modifier = Modifier,
                        tint = SsTheme.colors.primaryForeground
                    )
                }
            )
        }
    }

    TopAppBar(
        title = {
            val density = LocalDensity.current
            AnimatedVisibility(
                visible = collapsed,
                enter = slideInVertically {
                    with(density) { -40.dp.roundToPx() }
                } + expandVertically(
                    expandFrom = Alignment.Top
                ) + fadeIn(
                    initialAlpha = 0.3f
                ),
                exit = fadeOut(),
            ) {
                title()
            }
        },
        modifier = modifier,
        navigationIcon = {
            val containerColor by topAppBarContainerColor(collapsible, collapsed)
            val iconColor by topAppBarContentColor(collapsible, collapsed)
            IconButton(
                onClick = onNavBack,
                colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
            ) {
                IconBox(
                    icon = Icons.ArrowBack,
                    contentColor = iconColor,
                )
            }
        },
        actions = {
            buildList {
                actions.filter { it.primary }.forEach {
                    add(
                        IconButtonSlot(
                            imageVector = it.icon,
                            contentDescription = stringResource(it.title),
                            onClick = {},
                        )
                    )
                }
                if (actions.any { it.primary == false }) {
                    add(
                        IconButtonSlot(
                            imageVector = MaterialIcons.Rounded.MoreVert,
                            contentDescription = stringResource(L10nR.string.ss_more),
                            onClick = { expanded = true },
                        )
                    )
                }
            }.forEach { icon ->
                val containerColor by topAppBarContainerColor(collapsible, collapsed)
                val iconColor by topAppBarContentColor(collapsible, collapsed)
                IconButton(
                    onClick = icon.onClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
                ) {
                    IconBox(
                        icon = icon,
                        contentColor = iconColor,
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        )
    )
}

@Composable
private fun topAppBarContainerColor(
    collapsible: Boolean,
    collapsed: Boolean,
) = animateColorAsState(
    targetValue = when {
        !collapsible -> Color.Transparent
        collapsible && !collapsed -> Color.Black.copy(alpha = 0.5f)
        else -> Color.Transparent
    },
    label = "container color"
)

@Composable
private fun topAppBarContentColor(
    collapsible: Boolean,
    collapsed: Boolean,
) = animateColorAsState(
    targetValue = when {
        !collapsible -> SsTheme.colors.primaryForeground
        collapsible && !collapsed -> Color.White
        else -> SsTheme.colors.primaryForeground
    },
    label = "icon-color"
)

@Composable
internal fun DocumentTitleBar(
    segments: ImmutableList<Segment>,
    selectedSegment: Segment?,
    modifier: Modifier = Modifier,
    onSelection: (Segment) -> Unit = {},
) {
    if (segments.size == 1) {
        Text(
            text = segments.first().title,
            modifier = modifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        DocumentSegmentDropdown(
            segments = segments,
            selectedSegment = selectedSegment,
            modifier = modifier,
            onSelection = onSelection
        )
    }
}

@Composable
private fun DocumentSegmentDropdown(
    segments: ImmutableList<Segment>,
    selectedSegment: Segment?,
    modifier: Modifier = Modifier,
    onSelection: (Segment) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = SsTheme.colors.primaryBackground,
    ) {
        segments.forEach { segment ->
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onSelection(segment)
                },
                modifier = Modifier,
                text = {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = segment.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = SsTheme.colors.primaryForeground
                            )
                        },
                        supportingContent = {
                            segment.date?.dateDisplay()?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SsTheme.colors.secondaryForeground
                                )
                            }
                        }
                    )
                },
                trailingIcon = {
                    if (segment == selectedSegment) {
                        IconBox(Icons.Check)
                    }
                }
            )
        }
    }

    Row(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = selectedSegment?.title ?: "",
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        IconBox(Icons.ArrowDropDown)
    }
}
