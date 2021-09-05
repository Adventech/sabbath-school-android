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

package app.ss.media.playback.ui.nowPlaying.components

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.ss.media.R
import app.ss.media.playback.PlaybackConnection
import app.ss.media.playback.extensions.isError
import app.ss.media.playback.extensions.isPlayEnabled
import app.ss.media.playback.extensions.isPlaying
import app.ss.media.playback.extensions.playPause
import com.cryart.design.theme.Dimens

@Composable
internal fun PlayBackControls(
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    modifier: Modifier = Modifier,
    playbackConnection: PlaybackConnection
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.grid_6),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = {},
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_audio_icon_backward),
                contentDescription = "Rewind",
                tint = contentColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(46.dp))

        IconButton(
            onClick = { playbackConnection.mediaController?.playPause() },
            modifier = Modifier.size(46.dp),
        ) {
            val painter = when {
                playbackState.isPlaying -> painterResource(id = R.drawable.ic_audio_icon_pause)
                playbackState.isError -> rememberVectorPainter(Icons.Rounded.ErrorOutline)
                playbackState.isPlayEnabled -> painterResource(id = R.drawable.ic_audio_icon_play)
                else -> painterResource(id = R.drawable.ic_audio_icon_play)
            }
            Icon(
                painter = painter,
                tint = contentColor,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "Play/Pause"
            )
        }

        Spacer(modifier = Modifier.width(46.dp))

        IconButton(
            onClick = {},
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_audio_icon_forward),
                contentDescription = "Forward",
                tint = contentColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
