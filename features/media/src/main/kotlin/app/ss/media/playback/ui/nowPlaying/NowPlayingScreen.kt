/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package app.ss.media.playback.ui.nowPlaying

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.media.R
import app.ss.media.playback.extensions.NONE_PLAYBACK_STATE
import app.ss.media.playback.extensions.NONE_PLAYING
import app.ss.media.playback.model.PlaybackSpeed
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import app.ss.media.playback.ui.nowPlaying.ScreenDefaults.tintColor
import com.cryart.design.base.TransparentBottomSheetSurface
import com.cryart.design.theme.BaseBlue
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.TitleMedium
import com.cryart.design.widgets.DragHandle
import kotlinx.coroutines.flow.StateFlow

private object ScreenDefaults {

    @Composable
    fun tintColor(): Color =
        if (isSystemInDarkTheme()) {
            BaseGrey2
        } else {
            BaseBlue
        }
}

@Composable
internal fun NowPlayingScreen(
    viewModel: NowPlayingViewModel = viewModel(),
) {
    TransparentBottomSheetSurface {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = Spacing16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val playbackConnection = viewModel.playbackConnection
            val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
                .collectAsState(NONE_PLAYBACK_STATE)
            val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)
                .collectAsState(NONE_PLAYING)

            DragHandle()

            Spacer(modifier = Modifier.weight(1f))

            BottomControls(
                playbackSpeedFlow = playbackConnection.playbackSpeed,
                toggleSpeed = { playbackSpeed ->
                    playbackConnection.toggleSpeed(playbackSpeed)
                }
            )
        }
    }
}

@Composable
private fun BottomControls(
    modifier: Modifier = Modifier,
    playbackSpeedFlow: StateFlow<PlaybackSpeed>,
    toggleSpeed: (PlaybackSpeed) -> Unit = {}
) {
    val playbackSpeed by rememberFlowWithLifecycle(playbackSpeedFlow)
        .collectAsState(PlaybackSpeed.NORMAL)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.grid_4,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = {
                toggleSpeed(playbackSpeed)
            }
        ) {
            Text(
                text = playbackSpeed.label,
                style = TitleMedium.copy(
                    color = tintColor()
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = {}) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_icon_playlist),
                contentDescription = "PlayList",
                tint = tintColor()
            )
        }
    }
}
