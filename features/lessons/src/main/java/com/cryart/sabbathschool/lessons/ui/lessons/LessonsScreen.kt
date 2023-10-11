/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.cryart.sabbathschool.lessons.ui.lessons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import app.ss.design.compose.widget.scaffold.SsScaffold
import app.ss.models.LessonIntroModel
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonItemSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonItemsSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonsFooterSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.footer
import com.cryart.sabbathschool.lessons.ui.lessons.components.lessons
import com.cryart.sabbathschool.lessons.ui.lessons.components.loading
import com.cryart.sabbathschool.lessons.ui.lessons.components.quarterlyInfo
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.toSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.toSpec
import app.ss.translations.R.string as RString

@Composable
internal fun LessonsScreen(
    state: LessonsScreenState,
    onNavClick: () -> Unit,
    onShareClick: (String) -> Unit,
    onLessonClick: (LessonItemSpec) -> Unit,
    onReadMoreClick: (LessonIntroModel) -> Unit,
) {
    val listState: LazyListState = rememberLazyListState()
    val scrollAlpha: ScrollAlpha = rememberScrollAlpha(listState = listState)

    LessonsScreen(
        state = state,
        listState = listState,
        scrollAlpha = scrollAlpha,
        onNavClick = onNavClick,
        onShareClick = onShareClick,
        onLessonClick = onLessonClick,
        onReadMoreClick = onReadMoreClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LessonsScreen(
    state: LessonsScreenState,
    listState: LazyListState,
    scrollAlpha: ScrollAlpha,
    onNavClick: () -> Unit = {},
    onShareClick: (String) -> Unit = {},
    onLessonClick: (LessonItemSpec) -> Unit = {},
    onReadMoreClick: (LessonIntroModel) -> Unit = {},
) {
    val quarterlyTitle = remember(state.quarterlyTitle) { state.quarterlyTitle }
    val scrollCollapsed by remember { derivedStateOf { scrollAlpha.alpha > MIN_SOLID_ALPHA } }
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val iconTint by remember { derivedStateOf { Color.White.takeUnless { collapsed } } }

    SsScaffold(
        modifier = Modifier,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = SsTheme.colors.primaryBackground.copy(
                    alpha = if (scrollCollapsed) 1f else scrollAlpha.alpha
                ),
                tonalElevation = if (scrollCollapsed) 4.dp else 0.dp
            ) {
                LessonsTopBar(
                    title = quarterlyTitle,
                    showTitle = collapsed,
                    iconTint = iconTint,
                    modifier = Modifier,
                    onNavClick = onNavClick,
                    onShareClick = {
                        onShareClick(quarterlyTitle)
                    }
                )
            }
        },
    ) { innerPadding ->

        LessonsLazyColumn(
            quarterlyInfoState = state.quarterlyInfo,
            publishingInfoState = state.publishingInfo,
            innerPadding = innerPadding,
            listState = listState,
            onLessonClick = onLessonClick,
            onReadMoreClick = onReadMoreClick,
            modifier = Modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonsTopBar(
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
                    imageVector = Icons.Rounded.Share,
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
            IconBox(
                icon = IconButton(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(id = RString.ss_action_back),
                    onClick = onNavClick,
                    tint = iconTint
                )
            )
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun LessonsLazyColumn(
    quarterlyInfoState: QuarterlyInfoState,
    publishingInfoState: PublishingInfoState,
    innerPadding: PaddingValues,
    listState: LazyListState,
    onLessonClick: (LessonItemSpec) -> Unit,
    onReadMoreClick: (LessonIntroModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val quarterlyInfo = when (quarterlyInfoState) {
        QuarterlyInfoState.Error,
        QuarterlyInfoState.Loading -> null
        is QuarterlyInfoState.Success -> quarterlyInfoState.quarterlyInfo
    }

    val publishingInfo = when (publishingInfoState) {
        PublishingInfoState.Error,
        PublishingInfoState.Loading -> null
        is PublishingInfoState.Success -> publishingInfoState.publishingInfo
    }

    LazyColumn(
        contentPadding = PaddingValues(
            innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            0.dp,
            innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            innerPadding.calculateBottomPadding()
        ),
        modifier = modifier,
        state = listState
    ) {

        quarterlyInfo?.let { ssQuarterlyInfo ->
            val quarterly = ssQuarterlyInfo.quarterly
            quarterlyInfo(
                info = ssQuarterlyInfo.toSpec(
                    readMoreClick = {
                        onReadMoreClick(
                            LessonIntroModel(
                                index = quarterly.index,
                                title = quarterly.title,
                                introduction = quarterly.introduction ?: quarterly.description
                            )
                        )
                    }
                ),
                publishingInfo = publishingInfo?.toSpec(
                    primaryColorHex = ssQuarterlyInfo.quarterly.color_primary
                ),
                scrollOffset = { listState.firstVisibleItemScrollOffset.toFloat() },
                onLessonClick = onLessonClick
            )

            lessons(
                lessonsSpec = LessonItemsSpec(ssQuarterlyInfo.lessons.map { it.toSpec() }),
                onClick = onLessonClick
            )

            footer(
                spec = LessonsFooterSpec(
                    credits = quarterlyInfo.quarterly.credits.map { it.toSpec() },
                    features = quarterlyInfo.quarterly.features.map { it.toSpec() }
                ),
            )
        } ?: run {
            loading()
        }
    }
}

private const val MIN_SOLID_ALPHA = 0.8f

@Immutable
data class ScrollAlpha(
    val alpha: Float
)

@Composable
private fun rememberScrollAlpha(
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
