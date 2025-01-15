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

package ss.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.zIndex
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.FeedTopAppBar
import app.ss.design.compose.widget.scaffold.HazeScaffold
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.components.SingletonComponent
import ss.feed.components.FeedLazyColum
import ss.feed.components.view.FeedLoadingView
import ss.libraries.circuit.navigation.FeedScreen
import ss.services.auth.overlay.AccountDialogOverlay

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(FeedScreen::class, SingletonComponent::class)
@Composable
fun FeedScreenUi(state: State, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FeedTopAppBar(
                photoUrl = state.photoUrl,
                title = state.title,
                scrollBehavior = scrollBehavior,
                onNavigationClick = { state.eventSink(Event.ProfileClick) },
                onFilterLanguagesClick = { state.eventSink(Event.FilterLanguages) }
            )
        },
        blurTopBar = true,
    ) { contentPadding ->
        when (state) {
            is State.Loading -> {
                FeedLoadingView(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                )
            }

            is State.Group -> {
                FeedLazyColum(
                    groups = state.groups,
                    modifier = Modifier,
                    state = listState,
                    contentPadding = contentPadding,
                    seeAllClick = { state.eventSink(SuccessEvent.OnSeeAllClick(it)) },
                    itemClick = { state.eventSink(SuccessEvent.OnItemClick(it.index)) }
                )
            }

            is State.List -> {
                FeedLazyColum(
                    resources = state.resources,
                    modifier = Modifier,
                    state = listState,
                    contentPadding = contentPadding,
                    itemClick = { state.eventSink(SuccessEvent.OnItemClick(it)) }
                )
            }
        }
    }

    state.overlayState?.let { overlayState ->
        OverlayEffect(overlayState) {
            when (overlayState) {
                is OverlayState.AccountInfo -> overlayState.onResult(
                    show(AccountDialogOverlay(overlayState.userInfo))
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LoadingPreview() {
    SsTheme { Surface { FeedScreenUi(state = State.Loading("", null, null) {}) } }
}
