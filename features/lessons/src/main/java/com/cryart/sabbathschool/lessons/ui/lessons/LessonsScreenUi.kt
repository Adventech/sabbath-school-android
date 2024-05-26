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

package com.cryart.sabbathschool.lessons.ui.lessons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.scaffold.SsScaffold
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonItemsSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonsFooterSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.footer
import com.cryart.sabbathschool.lessons.ui.lessons.components.lessons
import com.cryart.sabbathschool.lessons.ui.lessons.components.loading
import com.cryart.sabbathschool.lessons.ui.lessons.components.quarterlyInfo
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.toSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.toSpec
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(ss.libraries.circuit.navigation.LessonsScreen::class, SingletonComponent::class)
@Composable
fun LessonsScreenUi(state: State, modifier: Modifier = Modifier) {
    val listState: LazyListState = rememberLazyListState()
    val scrollAlpha: ScrollAlpha = rememberScrollAlpha(listState = listState)
    val scrollCollapsed by remember { derivedStateOf { scrollAlpha.alpha > MIN_SOLID_ALPHA } }
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val iconTint by remember { derivedStateOf { Color.White.takeUnless { collapsed } } }

    SsScaffold(
        modifier = modifier,
        topBar = {
            (state as? State.Success)?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SsTheme.colors.primaryBackground.copy(
                        alpha = if (scrollCollapsed) 1f else scrollAlpha.alpha
                    ),
                    tonalElevation = if (scrollCollapsed) 4.dp else 0.dp
                ) {
                    LessonsTopBar(
                        title = state.quarterlyInfo.quarterly.title,
                        showTitle = collapsed,
                        iconTint = iconTint,
                        modifier = Modifier,
                        onNavClick = { state.eventSink(Event.OnNavigateBackClick) },
                        onShareClick = { state.eventSink(Event.OnShareClick) }
                    )
                }
            }

        },
    ) { innerPadding ->
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
            when (state) {
                is State.Loading,
                is State.Error -> loading()
                is State.Success -> {
                    val ssQuarterlyInfo = state.quarterlyInfo
                    val quarterly = ssQuarterlyInfo.quarterly
                    quarterlyInfo(
                        info = ssQuarterlyInfo.toSpec(
                            readMoreClick = {
                                state.eventSink(Event.OnReadMoreClick)
                            },
                            offlineStateClick = {
                                state.eventSink(Event.OnOfflineStateClick)
                            }
                        ),
                        publishingInfo = state.publishingInfo?.toSpec(
                            primaryColorHex = ssQuarterlyInfo.quarterly.color_primary
                        ),
                        scrollOffset = { listState.firstVisibleItemScrollOffset.toFloat() },
                        onLessonClick = { lesson -> state.eventSink(Event.OnLessonClick(lesson)) }
                    )

                    lessons(
                        lessonsSpec = LessonItemsSpec(ssQuarterlyInfo.lessons.map { it.toSpec() }),
                        onClick = { lesson -> state.eventSink(Event.OnLessonClick(lesson)) }
                    )

                    footer(
                        spec = LessonsFooterSpec(
                            credits = quarterly.credits.map { it.toSpec() },
                            features = quarterly.features.map { it.toSpec() }
                        ),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            LessonsScreenUi(state = State.Loading)
        }
    }
}
