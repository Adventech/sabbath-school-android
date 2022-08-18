/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

@file:OptIn(ExperimentalAnimationApi::class)

package app.ss.media.playback.ui.nowPlaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import app.ss.media.playback.extensions.isPlaying
import app.ss.media.playback.ui.nowPlaying.components.BoxState
import app.ss.media.playback.ui.nowPlaying.components.CoverImageStatic
import app.ss.media.playback.ui.nowPlaying.components.NowPlayingColumn
import app.ss.media.playback.ui.nowPlaying.components.PlaybackQueueList
import app.ss.media.playback.ui.spec.PlaybackQueueSpec
import app.ss.media.playback.ui.spec.toImageSpec
import app.ss.media.playback.ui.spec.toSpec

@Composable
internal fun NowPlayingDetail(
    spec: NowPlayingScreenSpec,
    boxState: BoxState,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = boxState,
        modifier = modifier,
        transitionSpec = {
            if (targetState == BoxState.Collapsed) {
                expandVertically { it } with fadeOut(animationSpec = tween(100))
            } else {
                scaleIn(
                    animationSpec = tween(500),
                    initialScale = 0.3f,
                    transformOrigin = TransformOrigin(0f, 0f)
                ) with fadeOut(animationSpec = tween(100))
            }.using(SizeTransform(clip = false))
        }
    ) {
        when (it) {
            BoxState.Collapsed -> {
                NowPlayingDetailCollapsed(
                    spec = spec,
                    boxState = boxState,
                    listState = listState
                )
            }
            BoxState.Expanded -> {
                NowPlayingDetailExpanded(
                    spec = spec,
                    boxState = boxState
                )
            }
        }
    }
}

@Composable
internal fun NowPlayingDetailExpanded(
    spec: NowPlayingScreenSpec,
    boxState: BoxState,
    modifier: Modifier = Modifier
) {
    val nowPlayingAudio = spec.nowPlayingAudio

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        CoverImageStatic(
            spec = nowPlayingAudio.toImageSpec(),
            boxState = boxState,
            modifier = Modifier
        )

        NowPlayingColumn(
            spec = nowPlayingAudio.toSpec(),
            boxState = boxState,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NowPlayingDetailCollapsed(
    spec: NowPlayingScreenSpec,
    boxState: BoxState,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val nowPlayingAudio = spec.nowPlayingAudio
    val playbackQueue = spec.playbackQueue
    val playbackState = spec.playbackState
    val playbackConnection = spec.playbackConnection

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImageStatic(
                spec = nowPlayingAudio.toImageSpec(),
                boxState = boxState,
                modifier = Modifier
            )

            NowPlayingColumn(
                spec = nowPlayingAudio.toSpec(),
                boxState = boxState,
                modifier = Modifier.weight(1f)
            )
        }

        PlaybackQueueList(
            spec = PlaybackQueueSpec(
                listState = listState,
                queue = playbackQueue.audiosList.map { it.toSpec() },
                nowPlayingId = nowPlayingAudio.id,
                isPlaying = playbackState.isPlaying,
                onPlayAudio = { position ->
                    playbackConnection.transportControls?.skipToQueueItem(position.toLong())
                }
            ),
            modifier = Modifier
                .padding(top = 16.dp)
                .weight(1f)
        )
    }
}
