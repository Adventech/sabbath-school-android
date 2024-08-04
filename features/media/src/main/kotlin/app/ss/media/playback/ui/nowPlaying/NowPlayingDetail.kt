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

package app.ss.media.playback.ui.nowPlaying

import android.view.MotionEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.SsTheme
import app.ss.media.playback.ui.nowPlaying.components.BoxState
import app.ss.media.playback.ui.nowPlaying.components.CoverImage
import app.ss.media.playback.ui.nowPlaying.components.NowPlayingColumn
import app.ss.media.playback.ui.nowPlaying.components.PlaybackQueueList
import app.ss.media.playback.ui.spec.PlaybackQueueSpec
import app.ss.media.playback.ui.spec.toImageSpec
import app.ss.media.playback.ui.spec.toSpec
import app.ss.models.media.AudioFile

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun NowPlayingDetail(
    spec: NowPlayingScreenSpec,
    boxState: BoxState,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val (nowPlayingAudio, playbackQueue, playbackState, _, playbackConnection, _, isDraggable) = spec

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {

        SharedTransitionLayout {
            AnimatedContent(boxState, label = "") { targetState ->
                when (targetState) {
                    BoxState.Collapsed -> {
                        RowContent(
                            nowPlayingAudio = nowPlayingAudio,
                            boxState = targetState,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this
                        )
                    }

                    BoxState.Expanded -> {
                        ColumnContent(
                            nowPlayingAudio = nowPlayingAudio,
                            boxState = targetState,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        SharedTransitionLayout {

            AnimatedVisibility(
                visible = boxState == BoxState.Collapsed,
                enter = fadeIn(spring(stiffness = Spring.StiffnessVeryLow)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutVertically { it },
                modifier = Modifier.weight(1f),
            ) {

                PlaybackQueueList(
                    spec = PlaybackQueueSpec(
                        listState = listState,
                        queue = playbackQueue.audiosList.map { it.toSpec() },
                        nowPlayingId = nowPlayingAudio.id,
                        isPlaying = playbackState.isPlaying,
                        onPlayAudio = { position ->
                            playbackConnection.skipToItem(position)
                        }
                    ),
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "queue-list-bounds"),
                            animatedVisibilityScope = this,
                        )
                        .padding(top = 16.dp)
                        .padding(horizontal = 4.dp)
                        .pointerInteropFilter { event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    if (listState.firstVisibleItemIndex > 0) {
                                        isDraggable(false)
                                    }
                                }

                                MotionEvent.ACTION_UP -> {
                                    isDraggable(true)
                                }
                            }
                            false
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RowContent(
    nowPlayingAudio: AudioFile,
    boxState: BoxState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.grid_4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        with(sharedTransitionScope) {
            CoverImage(
                spec = nowPlayingAudio.toImageSpec(),
                boxState = boxState,
                modifier = Modifier
                    .sharedElement(
                        rememberSharedContentState(key = "image"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            spring(dampingRatio = 0.8f, stiffness = 380f)
                        },
                    ),
            )

            NowPlayingColumn(
                spec = nowPlayingAudio.toSpec(),
                boxState = boxState,
                sharedTransitionScope = this,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ColumnContent(
    nowPlayingAudio: AudioFile,
    boxState: BoxState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        with(sharedTransitionScope) {
            CoverImage(
                spec = nowPlayingAudio.toImageSpec(),
                boxState = boxState,
                modifier = Modifier
                    .sharedElement(
                        rememberSharedContentState(key = "image"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            spring(dampingRatio = 0.8f, stiffness = 380f)
                        },
                    ),
            )

            NowPlayingColumn(
                spec = nowPlayingAudio.toSpec(),
                boxState = boxState,
                sharedTransitionScope = this,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .padding(horizontal = Dimens.grid_4)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NowPlayingDetailPreview() {
    SsTheme {
        Surface {
            NowPlayingDetail(
                spec = PreviewData.nowPlayScreenSpec(),
                boxState = BoxState.Collapsed,
                listState = rememberLazyListState()
            )
        }
    }
}
