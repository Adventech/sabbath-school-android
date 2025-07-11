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
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.translations.R as L10nR
import ss.resource.R as ResourceR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResourceTopAppBar(
    isShowingNavigationBar: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    showShare: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavBack: () -> Unit = {},
    onShareClick: () -> Unit = {},
) {
    val contentColor by topAppBarContentColor()

    TopAppBar(
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
            IconButton(onClick = onNavBack) {
                AnimatedAppBarIcon(
                    targetState = isShowingNavigationBar,
                    icon = {
                        Icon(
                            painter = painterResource(ResourceR.drawable.ic_arrow_backward),
                            contentDescription = stringResource(L10nR.string.ss_action_arrow_back),
                            tint = contentColor,
                        )
                    },
                    boxIcon = {
                        IconBox(
                            icon = Icons.ArrowBack,
                            contentColor = Color.White,
                        )
                    },
                    modifier = Modifier,
                    iconTint = iconTint
                )
            }
        },
        actions = {
            if (showShare) {
                IconButton(onClick = onShareClick) {
                    AnimatedAppBarIcon(
                        targetState = isShowingNavigationBar,
                        icon = {
                            IconBox(
                                icon = Icons.Share,
                                contentColor = contentColor,
                            )
                        },
                        boxIcon = {
                            IconBox(
                                icon = Icons.Share,
                                contentColor = Color.White,
                                modifier = Modifier,
                            )
                        },
                        modifier = Modifier,
                        iconTint = iconTint
                    )
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
    isNightMode: Boolean = isSystemInDarkTheme(),
) = animateColorAsState(
    if (isNightMode) Color.White else Color.Black,
    label = "content color"
)

@Composable
private fun AnimatedAppBarIcon(
    targetState: Boolean,
    icon: @Composable () -> Unit,
    boxIcon: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
    ) { isVisible ->
        if (isVisible) {
            icon()
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconTint ?: Color.Black.copy(0.6f), CircleShape),
                contentAlignment = Alignment.Center,
                content = boxIcon
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            ResourceTopAppBar(
                isShowingNavigationBar = true,
                title = "Resource",
                showShare = true,
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
                showShare = true
            )
        }
    }
}
