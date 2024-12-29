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

package ss.resource

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.scroll.ScrollAlpha
import app.ss.design.compose.extensions.scroll.rememberScrollAlpha
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.scaffold.HazeScaffold
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import ss.libraries.circuit.navigation.ResourceScreen
import ss.resource.components.CoverContent
import ss.resource.components.ResourceCover
import ss.resource.components.ResourceLoadingView
import ss.resource.components.ResourceTopAppBar
import ss.resource.components.footer
import ss.resource.components.footerBackgroundColor
import ss.resource.components.resourceSections

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(ResourceScreen::class, SingletonComponent::class)
@Composable
fun ResourceUi(state: State, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState: LazyListState = rememberLazyListState()
    val scrollAlpha: ScrollAlpha = rememberScrollAlpha(listState = listState)
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SsTheme.colors.primaryBackground.copy(
                    alpha = if (collapsed) 1f else scrollAlpha.alpha
                ),
                tonalElevation = if (collapsed) 4.dp else 0.dp
            ) {
                ResourceTopAppBar(
                    isShowingNavigationBar = collapsed,
                    title = state.title,
                    modifier = Modifier,
                    scrollBehavior = scrollBehavior,
                    onNavBack = { state.eventSink(Event.OnNavBack) }
                )
            }

        },
    ) {
        val color by animateColorAsState(
            targetValue = if (state is State.Success) {
                footerBackgroundColor()
            } else {
                SsTheme.colors.primaryBackground
            }, label = "background-color"
        )

        when (state) {
            is State.Loading -> {
                ResourceLoadingView(state = listState)
            }

            is State.Success -> {
                val resource = state.resource

                CompositionLocalProvider(
                    LocalFontFamilyProvider provides state.fontFamilyProvider,
                ) {
                    LazyColumn(
                        modifier = Modifier.background(color),
                        state = listState,
                    ) {
                        item("cover") {
                            ResourceCover(
                                resource = resource,
                                modifier = Modifier,
                                scrollOffset = { listState.firstVisibleItemScrollOffset.toFloat() },
                                content = { CoverContent(resource, it) }
                            )
                        }

                        resourceSections(state.sections)

                        footer(state.credits, state.features)
                    }
                }
            }
        }
    }
}
