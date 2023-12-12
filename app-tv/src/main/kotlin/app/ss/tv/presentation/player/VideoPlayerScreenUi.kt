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

package app.ss.tv.presentation.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import app.ss.tv.presentation.extentions.handleDPadKeyEvents
import app.ss.tv.presentation.player.VideoPlayerScreen.Event
import app.ss.tv.presentation.player.VideoPlayerScreen.State
import app.ss.tv.presentation.player.components.ControlsIconSize
import app.ss.tv.presentation.player.components.VideoPlayerControls
import app.ss.tv.presentation.player.components.VideoPlayerControlsIcon
import app.ss.translations.R as L10nR

@Composable
fun VideoPlayerScreenUi(
    state: State,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        when (state) {
            is State.Loading -> Unit
            is State.Playing -> PlayingUi(state)
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun BoxScope.PlayingUi(
    state: State.Playing
) {
    val context = LocalContext.current
    val videoPlayerState = rememberVideoPlayerState()

    AndroidView(
        modifier = Modifier
            .handleDPadKeyEvents(
                onEnter = {
                    if (videoPlayerState.isDisplayed) {
                        state.controls.onPlayPauseToggle()
                    } else {
                        videoPlayerState.showControls()
                    }
                },
            )
            .focusable(),
        factory = {
            PlayerView(context).apply {
                hideController()
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }.also { state.eventSink(Event.OnPlayerViewCreated(it)) }
        }
    )

    VideoPlayerControls(
        spec = state.controls,
        videoPlayerState = videoPlayerState,
        modifier = Modifier.align(Alignment.BottomCenter),
    )

    if (videoPlayerState.isDisplayed) {
        PlayPauseButton(
            isBuffering = state.controls.isBuffering,
            isPlaying = state.controls.isPlaying,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun PlayPauseButton(
    isBuffering: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isBuffering) {
        CircularProgressIndicator(
            modifier = modifier
                .size(ControlsIconSize)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    } else {
        VideoPlayerControlsIcon(
            isPlaying = isPlaying,
            contentDescription = stringResource(id = L10nR.string.ss_action_play_pause),
            modifier = modifier,
        )
    }
}
