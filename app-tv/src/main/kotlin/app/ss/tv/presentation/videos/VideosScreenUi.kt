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

package app.ss.tv.presentation.videos

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyListScope
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.Border
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.CardLayoutDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardLayout
import androidx.tv.material3.Text
import app.ss.tv.presentation.extentions.asPlaceholder
import app.ss.tv.presentation.theme.BorderWidth
import app.ss.tv.presentation.theme.Padding
import app.ss.tv.presentation.theme.SSTvTheme
import app.ss.tv.presentation.theme.SsCardShape
import app.ss.tv.presentation.theme.rememberChildPadding
import app.ss.tv.presentation.utils.FocusGroup
import app.ss.tv.presentation.videos.VideosScreen.Event
import app.ss.tv.presentation.videos.VideosScreen.State
import app.ss.tv.presentation.videos.ui.CategoryVideos

@Composable
fun VideosScreenUi(state: State, modifier: Modifier = Modifier) {
    val tvLazyListState = rememberTvLazyListState()
    val shouldShowTopBar by remember {
        derivedStateOf {
            tvLazyListState.firstVisibleItemIndex == 0 &&
                tvLazyListState.firstVisibleItemScrollOffset < 300
        }
    }

    var focusIndex by remember { mutableIntStateOf(-1) }

    TvLazyColumn(
        modifier = modifier.fillMaxSize(),
        state = tvLazyListState,
    ) {
        when (state) {
            is State.Error -> errorItem()
            is State.Loading -> loadingItem()
            is State.Videos -> {
                itemsIndexed(state.categories, key = { _, spec -> spec.id }) { index, spec ->
                    CategoryVideos(
                        category = spec,
                        isListFocused = focusIndex == index,
                        modifier = Modifier.onFocusChanged {
                            focusIndex = if (it.hasFocus) index else -1
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
                modifier = Modifier
                    .fillMaxSize()
                    .height(LocalConfiguration.current.screenHeightDp.times(0.2f).dp)
            )
        }
    }

    LaunchedEffect(shouldShowTopBar) {
        (state as? State.Videos)?.eventSink?.invoke(Event.OnScroll(shouldShowTopBar))
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
    ) { targetCount ->
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

@Preview(device = Devices.TV_1080p)
@Composable
private fun VideosPreview() {
    SSTvTheme {
        VideosScreenUi(state = State.Loading)
    }
}
