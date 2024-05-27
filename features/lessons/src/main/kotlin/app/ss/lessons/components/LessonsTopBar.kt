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

package app.ss.lessons.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import app.ss.design.compose.widget.icon.Icons
import app.ss.translations.R.string as RString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LessonsTopBar(
    title: String,
    showTitle: Boolean,
    modifier: Modifier = Modifier,
    iconTint: Color? = SsTheme.colors.primaryForeground,
    onNavClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
) {
    SsTopAppBar(
        spec = TopAppBarSpec(
            topAppBarType = TopAppBarType.Small,
            actions = listOf(
                IconButton(
                    imageVector = androidx.compose.material.icons.Icons.Rounded.Share,
                    contentDescription = stringResource(id = RString.ss_share),
                    onClick = onShareClick,
                    tint = iconTint
                )
            ).takeIf { title.isNotEmpty() } ?: emptyList()
        ),
        title = {
            val density = LocalDensity.current
            AnimatedVisibility(
                visible = showTitle,
                enter = slideInVertically {
                    with(density) { -40.dp.roundToPx() }
                } + expandVertically(
                    expandFrom = Alignment.Top
                ) + fadeIn(
                    initialAlpha = 0.3f
                ),
                exit = fadeOut()
            ) {
                Text(
                    text = title,
                    color = SsTheme.colors.primaryForeground,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onNavClick) {
                IconBox(icon = Icons.ArrowBack, contentColor = iconTint ?: LocalContentColor.current)
            }
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

internal const val MIN_SOLID_ALPHA = 0.8f

@Immutable
internal data class ScrollAlpha(
    val alpha: Float
)

@Composable
internal fun rememberScrollAlpha(
    listState: LazyListState
): ScrollAlpha {
    val scrollAlpha by remember {
        derivedStateOf {
            ScrollAlpha(alpha = listState.scrollAlpha())
        }
    }
    return scrollAlpha
}

private fun LazyListState.scrollAlpha(): Float {
    return when (firstVisibleItemIndex) {
        0 -> {
            val size = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: run {
                return 0f
            }
            (firstVisibleItemScrollOffset.toFloat() / size.toFloat()).coerceIn(0f, 1f)
        }
        else -> 1f
    }
}
