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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.cryart.sabbathschool.lessons.ui.lessons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import app.ss.models.SSLesson
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonsFooterSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.footer
import com.cryart.sabbathschool.lessons.ui.lessons.components.lessons
import com.cryart.sabbathschool.lessons.ui.lessons.components.loading
import com.cryart.sabbathschool.lessons.ui.lessons.components.quarterlyInfo
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.toSpec
import com.cryart.sabbathschool.lessons.ui.lessons.intro.LessonIntroModel
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel = viewModel(),
    onNavClick: () -> Unit,
    onShareClick: (String) -> Unit,
    onLessonClick: (SSLesson) -> Unit,
    onReadMoreClick: (LessonIntroModel) -> Unit
) {
    val state by rememberFlowWithLifecycle(flow = viewModel.uiState)
        .collectAsState(initial = LessonsScreenState())

    LessonsScreen(
        state = state,
        onNavClick = onNavClick,
        onShareClick = onShareClick,
        onLessonClick = onLessonClick,
        onReadMoreClick = onReadMoreClick,
        systemUiController = rememberSystemUiController()
    )
}

@Composable
internal fun LessonsScreen(
    state: LessonsScreenState,
    scrollState: TopAppBarScrollState = rememberTopAppBarScrollState(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        state = scrollState,
        canScroll = { true },
    ),
    onNavClick: () -> Unit = {},
    onShareClick: (String) -> Unit = {},
    onLessonClick: (SSLesson) -> Unit = {},
    onReadMoreClick: (LessonIntroModel) -> Unit = {},
    systemUiController: SystemUiController? = null,
    listState: LazyListState = rememberLazyListState(),
    scrollAlpha: ScrollAlpha = rememberScrollAlpha(listState = listState),
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

    val alpha = if (scrollAlpha.alpha > MIN_SOLID_ALPHA) 1f else scrollAlpha.alpha
    val elevation = if (scrollAlpha.alpha > MIN_SOLID_ALPHA) 4.dp else 0.dp

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
                    systemUiController = systemUiController,
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
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
            ),
            state = listState,
        ) {

            quarterlyInfo?.let { ssQuarterlyInfo ->
                quarterlyInfo(
                    info = ssQuarterlyInfo,
                    publishingInfo = publishingInfo,
                    scrollOffset = listState.firstVisibleItemScrollOffset.toFloat(),
                    onReadMoreClick = onReadMoreClick,
                    onLessonClick = onLessonClick,
                )

                lessons(
                    lessons = ssQuarterlyInfo.lessons,
                    onClick = onLessonClick
                )

                footer(
                    spec = LessonsFooterSpec(
                        credits = quarterlyInfo.quarterly.credits.map { it.toSpec() },
                        features = quarterlyInfo.quarterly.features.map { it.toSpec() },
                    )
                )

                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                }
            } ?: run {
                loading()
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
    systemUiController: SystemUiController? = null,
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
            val density = LocalDensity.current
            AnimatedVisibility(
                visible = scrollAlpha == 1f,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
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
        systemUiController?.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
}

@Stable
@Composable
private fun toolbarIconColor(alpha: Float): Color {
    return if (alpha > MIN_SOLID_ALPHA) MaterialTheme.colorScheme.onSurface
    else Color.White
}

private const val MIN_SOLID_ALPHA = 0.8f

internal data class ScrollAlpha(
    val alpha: Float,
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
