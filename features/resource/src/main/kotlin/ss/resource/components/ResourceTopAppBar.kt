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

package ss.resource.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResourceTopAppBar(
    isShowingNavigationBar: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavBack: () -> Unit = {},
) {
    SsTopAppBar(
        spec = TopAppBarSpec(TopAppBarType.Small),
        modifier = modifier,
        title = {
            val density = LocalDensity.current
            AnimatedVisibility(
                visible = isShowingNavigationBar,
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
        navigationIcon = {
            val contentColor by topAppBarContentColor(isShowingNavigationBar, iconTint)

            IconButton(onClick = onNavBack) {
                AnimatedContent(
                    targetState = if (isShowingNavigationBar) {
                        Icons.ArrowBack
                    } else {
                        Icons.ArrowBackFilled
                    }
                ) { icon ->
                    IconBox(icon = icon, contentColor = contentColor)
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        )
    )
}

@Stable
@Composable
private fun topAppBarContentColor(
    isShowingNavigationBar: Boolean,
    iconTint: Color?,
    isNightMode: Boolean = isSystemInDarkTheme(),
) = animateColorAsState(
    if (isShowingNavigationBar) {
        if (isNightMode) Color.White else Color.Black
    } else {
        iconTint ?: SsTheme.colors.primaryForeground
    },
    label = "content color"
)

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            ResourceTopAppBar(
                isShowingNavigationBar = true,
                title = "Resource",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun PreviewTwo() {
    SsTheme {
        Surface {
            ResourceTopAppBar(
                isShowingNavigationBar = false,
                title = "Resource",
            )
        }
    }
}
