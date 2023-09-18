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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.Dimens
import app.ss.media.playback.ui.nowPlaying.components.BoxState
import app.ss.media.playback.ui.nowPlaying.components.CoverImage
import app.ss.media.playback.ui.nowPlaying.components.NowPlayingColumn
import app.ss.media.playback.ui.nowPlaying.components.PlaybackQueueList
import app.ss.media.playback.ui.spec.PlaybackQueueSpec
import app.ss.media.playback.ui.spec.toImageSpec
import app.ss.media.playback.ui.spec.toSpec

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun NowPlayingDetail(
    spec: NowPlayingScreenSpec,
    boxState: BoxState,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val (nowPlayingAudio, playbackQueue, playbackState, _, playbackConnection, _, isDraggable) = spec
    val expanded = boxState == BoxState.Expanded
    var imageSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    val alignment = if (expanded) Alignment.TopCenter else Alignment.TopStart
    val paddingTop by animateDpAsState(
        if (expanded) 64.dp else 0.dp,
        animationSpec = spring(
            Spring.DampingRatioMediumBouncy,
            stiffness = if (expanded) Spring.StiffnessVeryLow else Spring.StiffnessHigh
        ), label = "top-padding"
    )
    val textPaddingTop by animateDpAsState(
        targetValue = if (expanded) imageSize.height.plus(16.dp) else
            imageSize.height.div(4).minus(16.dp),
        animationSpec = if (expanded) tween(50) else spring(
            Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "text-padding-top"
    )
    val textPaddingStart by animateDpAsState(
        targetValue = if (expanded) 0.dp else imageSize.width,
        label = "text-padding-start"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(top = paddingTop.coerceAtLeast(0.dp))
    ) {

        CoverImage(
            spec = nowPlayingAudio.toImageSpec(),
            boxState = boxState,
            modifier = Modifier
                .align(alignment)
                .padding(horizontal = Dimens.grid_4),
            heightCallback = {
                imageSize = it
            }
        )

        NowPlayingColumn(
            spec = nowPlayingAudio.toSpec(),
            boxState = boxState,
            modifier = Modifier
                .padding(
                    start = textPaddingStart,
                    top = textPaddingTop.coerceAtLeast(0.dp)
                )
                .padding(horizontal = Dimens.grid_4)
                .align(alignment)
        )

        AnimatedVisibility(
            visible = !expanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessVeryLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessHigh))
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
                    .padding(top = imageSize.height.plus(16.dp))
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
