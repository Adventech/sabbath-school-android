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

package app.ss.lessons

import android.widget.TextView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.ss.design.compose.extensions.color.toAndroidColor
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.scaffold.SsScaffold
import app.ss.lessons.components.LessonItemsSpec
import app.ss.lessons.components.LessonsTopBar
import app.ss.lessons.components.MIN_SOLID_ALPHA
import app.ss.lessons.components.ScrollAlpha
import app.ss.lessons.components.footer
import app.ss.lessons.components.footerBackgroundColor
import app.ss.lessons.components.lessons
import app.ss.lessons.components.loading
import app.ss.lessons.components.quarterlyInfo
import app.ss.lessons.components.rememberScrollAlpha
import app.ss.lessons.components.spec.toSpec
import app.ss.lessons.components.toSpec
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.LocalOverlayHost
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.Markwon
import kotlinx.collections.immutable.toImmutableList
import ss.libraries.circuit.navigation.LessonsScreen
import ss.libraries.circuit.overlay.BottomSheetOverlay
import com.cryart.design.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(LessonsScreen::class, SingletonComponent::class)
@Composable
fun LessonsScreenUi(state: State, modifier: Modifier = Modifier) {
    val listState: LazyListState = rememberLazyListState()
    val scrollAlpha: ScrollAlpha = rememberScrollAlpha(listState = listState)
    val scrollCollapsed by remember { derivedStateOf { scrollAlpha.alpha > MIN_SOLID_ALPHA } }
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val iconTint by remember { derivedStateOf { Color.White.takeUnless { collapsed } } }
    val context = LocalContext.current

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
                        onShareClick = { state.eventSink(Event.OnShareClick(context)) }
                    )
                }
            }

        },
    ) { innerPadding ->
        val color by animateColorAsState(
            targetValue = if (state is State.Success) {
                footerBackgroundColor()
            } else {
                SsTheme.colors.primaryBackground
            }, label = "background-color"
        )

        LazyColumn(
            contentPadding = PaddingValues(
                innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                0.dp,
                innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                innerPadding.calculateBottomPadding()
            ),
            modifier = modifier.background(color),
            state = listState,
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
                        onLessonClick = { lesson -> state.eventSink(Event.OnLessonClick(lesson)) },
                        onPublishingInfoClick = { state.eventSink(Event.OnPublishingInfoClick) }
                    )

                    lessons(
                        lessonsSpec = LessonItemsSpec(ssQuarterlyInfo.lessons.map { it.toSpec() }),
                        onClick = { lesson -> state.eventSink(Event.OnLessonClick(lesson)) }
                    )

                    footer(
                        credits = quarterly.credits.map { it.toSpec() }.toImmutableList(),
                        features = quarterly.features.map { it.toSpec() }.toImmutableList()
                    )
                }
            }
        }

        (state as? State.Success)?.overlayState?.run { OverlayContent(this) }
    }
}

@Composable
private fun OverlayContent(state: ReadMoreOverlayState) {
    val overlayHost = LocalOverlayHost.current
    LaunchedEffect(state) {
        val result = overlayHost.show(
            BottomSheetOverlay(
                skipPartiallyExpanded = true,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        MarkdownText(
                            text = state.content,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            )
        )
        when (result) {
            BottomSheetOverlay.Result.Dismissed -> state.onResult(ReadMoreOverlayState.Result.Dismissed)
        }
    }
}

@Composable
private fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val markwon = remember { Markwon.create(context) }
    val contentColor = SsTheme.colors.primaryForeground

    AndroidView(
        factory = { _ ->
            TextView(context).apply {
                markwon.setMarkdown(this, text)
                setTextColor(contentColor.toAndroidColor())
                setTextAppearance(DesignR.style.TextAppearance_Markdown)
            }
        },
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun PreviewLoading() {
    SsTheme {
        Surface {
            LessonsScreenUi(state = State.Loading)
        }
    }
}
