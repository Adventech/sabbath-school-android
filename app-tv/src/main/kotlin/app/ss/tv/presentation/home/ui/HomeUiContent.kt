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

package app.ss.tv.presentation.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyListScope
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.Border
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.CardLayoutDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardLayout
import androidx.tv.material3.Text
import app.ss.tv.presentation.extentions.asPlaceholder
import app.ss.tv.presentation.home.HomeScreen.Event
import app.ss.tv.presentation.home.HomeScreen.State
import app.ss.tv.presentation.theme.BorderWidth
import app.ss.tv.presentation.theme.Padding
import app.ss.tv.presentation.theme.SsCardShape
import app.ss.tv.presentation.theme.rememberChildPadding
import app.ss.tv.presentation.utils.FocusGroup

@Composable
fun HomeUiContent(
    state: State,
    modifier: Modifier = Modifier
) {
    val tvLazyListState = rememberTvLazyListState()

    val pivotOffset = remember { PivotOffsets() }
    val pivotOffsetForImmersiveList = remember { PivotOffsets(0f, 0f) }
    var immersiveListHasFocus by remember { mutableStateOf(false) }

    TvLazyColumn(
        modifier = modifier.fillMaxSize(),
        pivotOffsets = if (immersiveListHasFocus) pivotOffsetForImmersiveList else pivotOffset,
        state = tvLazyListState
    ) {

        when (state) {
            State.Error -> errorItem()
            State.Loading -> loadingItem()
            is State.Videos -> {
                items(state.categories, key = { it.id }) { spec ->
                    CategoryVideos(
                        category = spec,
                        modifier = Modifier.onFocusChanged {
                            immersiveListHasFocus = it.hasFocus
                        },
                        onVideoClick = {
                            state.eventSink(Event.OnVideoClick(it))
                        },
                    )
                }
            }
        }

        item {
            Spacer(
                modifier = Modifier.padding(
                    bottom = LocalConfiguration.current.screenHeightDp.dp.times(0.19f)
                )
            )
        }
    }
}

private fun TvLazyListScope.errorItem() {
    item {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun TvLazyListScope.loadingItem() {
    item { Spacer(modifier = Modifier.padding(top = 54.dp)) }

    item { LoadingRow(count = 8) }

    item { LoadingRow(count = 4) }

    item { LoadingRow(count = 10) }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun LoadingRow(
    count: Int,
    modifier: Modifier = Modifier,
    childPadding: Padding = rememberChildPadding(),
) {
    AnimatedContent(
        targetState = count,
        modifier = modifier
            .padding(top = 48.dp)
            .focusGroup(),
        label = "",
    ) {targetCount ->
        FocusGroup {
            TvLazyRow(
                pivotOffsets = PivotOffsets(parentFraction = 0.07f),
                contentPadding = PaddingValues(start = childPadding.start, end = childPadding.end)
            ) {
                items(targetCount) {
                    LoadingCard(modifier = Modifier.restorableFocus())
                }
            }
        }
    }
}

@Composable
private fun LoadingCard(
    modifier: Modifier = Modifier,
) {
    StandardCardLayout(
        imageCard = {
            CardLayoutDefaults.ImageCard(
                onClick = { },
                shape = CardDefaults.shape(SsCardShape),
                border = CardDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(
                            width = BorderWidth,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = SsCardShape
                    )
                ),
                scale = CardDefaults.scale(focusedScale = 1f),
                interactionSource = it
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .asPlaceholder(true, SsCardShape)
                        .aspectRatio(ASPECT_RATIO)
                        .padding(end = 16.dp)
                )
            }
        },
        title = {},
        modifier = modifier
            .width(CARD_WIDTH.dp)
            .padding(end = 16.dp)
            .then(modifier),
    )
}

const val ASPECT_RATIO = 16f / 9f
const val CARD_WIDTH = 260
