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
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconButtonSlot
import app.ss.design.compose.widget.navigation.AvatarNavigationIcon
import app.ss.design.compose.widget.scaffold.HazeScaffold
import app.ss.translations.R.string as L10nR
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import ss.feed.components.FeedGroupList
import ss.feed.components.view.FeedLoadingView
import ss.libraries.circuit.navigation.FeedScreen
import androidx.compose.material.icons.Icons as MaterialIcons

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(FeedScreen::class, SingletonComponent::class)
@Composable
fun FeedScreenUi(state: State, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SsTopAppBar(
                spec = TopAppBarSpec(
                    topAppBarType = TopAppBarType.Large,
                    actions = listOf(
                        IconButtonSlot(
                            imageVector = MaterialIcons.Rounded.Translate,
                            contentDescription = stringResource(id = L10nR.ss_quarterlies_filter_languages),
                            onClick = { state.eventSink(Event.FilterLanguages) }
                        )
                    )
                ),
                modifier = Modifier,
                navigationIcon = {
                    AvatarNavigationIcon(
                        photoUrl = state.photoUrl,
                        onClick = { state.eventSink(Event.ProfileClick) }
                    )
                },
                title = {
                    when (state) {
                        is State.Loading -> Unit
                        is State.Success -> {
                            Text(text = state.title)
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                )
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

            is State.Success -> {
                FeedGroupList(
                    groups = state.groups,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                    seeAllClick = { state.eventSink(SuccessEvent.OnSeeAllClick(it)) },
                    itemClick = { state.eventSink(SuccessEvent.OnItemClick(it)) }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LoadingPreview() {
    SsTheme { Surface { FeedScreenUi(state = State.Loading(null) {}) } }
}
