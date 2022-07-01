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

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.cryart.sabbathschool.lessons.ui.lessons

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarScrollState
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.design.compose.extensions.flow.rememberFlowWithLifecycle
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import app.ss.design.compose.widget.scaffold.SsScaffold
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.ui.lessons.components.PublishingInfo
import com.cryart.sabbathschool.lessons.ui.lessons.components.QuarterlyInfo
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.toSpec
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel = viewModel(),
    onNavClick: () -> Unit,
    onShareClick: (String) -> Unit,
) {
    val state by rememberFlowWithLifecycle(flow = viewModel.uiState)
        .collectAsState(initial = LessonsScreenState())

    LessonsScreen(
        state = state,
        onNavClick = onNavClick,
        onShareClick = onShareClick,
    )
}

@Composable
fun LessonsScreen(
    state: LessonsScreenState,
    scrollState: TopAppBarScrollState = rememberTopAppBarScrollState(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        state = scrollState,
        canScroll = { true },
    ),
    onNavClick: () -> Unit = {},
    onShareClick: (String) -> Unit = {},
) {
    val quarterlyInfo = when (state.quarterlyInfo) {
        QuarterlyInfoState.Error,
        QuarterlyInfoState.Loading -> null
        is QuarterlyInfoState.Success -> state.quarterlyInfo.quarterlyInfo
    }

    val publishingInfo = when (state.publishingInfo) {
        PublishingInfoState.Error,
        PublishingInfoState.Loading -> null
        is PublishingInfoState.Success -> state.publishingInfo.publishingInfo
    }

    val quarterlyTitle = quarterlyInfo?.quarterly?.title ?: ""

    val scrollAlpha = scrollBehavior.scrollAlpha()
    val alpha by animateFloatAsState(
        targetValue = if (scrollAlpha > MIN_SOLID_ALPHA) 1f else scrollAlpha
    )
    val elevation by animateDpAsState(
        targetValue = if (scrollAlpha > MIN_SOLID_ALPHA) 4.dp else 0.dp
    )

    SsScaffold(
        modifier = Modifier,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(
                    alpha = alpha
                ),
                tonalElevation = elevation,
            ) {
                LessonsTopBar(
                    title = quarterlyTitle,
                    modifier = Modifier.padding(
                        top = with(LocalDensity.current) {
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top).getTop(this).toDp()
                        },
                    ),
                    scrollAlpha = alpha,
                    scrollBehavior = scrollBehavior,
                    onNavClick = onNavClick,
                    onShareClick = {
                        onShareClick(quarterlyTitle)
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior,
    ) { innerPadding ->

        LazyColumn(
            contentPadding = PaddingValues(
                innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                0.dp,
                innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                innerPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
            )
        ) {

            quarterlyInfo?.let {
                item {
                    QuarterlyInfo(spec = it.toSpec())
                }

                publishingInfo?.let {
                    item {
                        PublishingInfo(
                            spec = it.toSpec(),
                            primaryColorHex = quarterlyInfo.quarterly.color_primary,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }

            val list = (0..50).map { it.toString() }
            items(count = list.size) {
                Text(
                    text = "Hello world ${list[it]}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                )
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }
    }
}

@Composable
private fun LessonsTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    scrollAlpha: Float = 0.0f,
    systemUiController: SystemUiController = rememberSystemUiController(),
    onNavClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
) {

    SsTopAppBar(
        spec = TopAppBarSpec(
            topAppBarType = TopAppBarType.Small,
            actions = listOf(
                IconButton(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(id = R.string.ss_share),
                    onClick = onShareClick,
                    tint = toolbarIconColor(alpha = scrollAlpha)
                )
            ).takeIf { title.isNotEmpty() } ?: emptyList(),
        ),
        title = {
            Text(
                text = title,
                modifier = Modifier.alpha(scrollAlpha),
                color = toolbarContentColor(alpha = scrollAlpha),
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconBox(
                icon = IconButton(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back", // todo: Add strings
                    onClick = onNavClick,
                    tint = toolbarIconColor(alpha = scrollAlpha)
                )
            )
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color.Transparent,
        )
    )

    val useDarkIcons = when {
        isSystemInDarkTheme() -> false
        else -> scrollAlpha > MIN_SOLID_ALPHA
    }

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
}

@Stable
@Composable
private fun toolbarContentColor(alpha: Float): Color {
    return if (alpha > MIN_SOLID_ALPHA) MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    else Color.White.copy(alpha = 1f - alpha)
}

@Stable
@Composable
private fun toolbarIconColor(alpha: Float): Color {
    return if (alpha > MIN_SOLID_ALPHA) MaterialTheme.colorScheme.onSurface
    else Color.White
}

private fun TopAppBarScrollBehavior.scrollAlpha(): Float {
    return 1f - (state.offsetLimit / (state.contentOffset.coerceAtMost(-0.9f))).coerceAtLeast(0f)
        .coerceIn(0f, 1f)
}

private const val MIN_SOLID_ALPHA = 0.8f
