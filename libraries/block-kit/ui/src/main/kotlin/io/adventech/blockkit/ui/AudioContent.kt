/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package io.adventech.blockkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.media.MediaPlayer
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.extensions.millisToDuration
import ss.services.media.ui.PlaybackPlayPause
import ss.services.media.ui.common.PlaybackSlider
import ss.services.media.ui.spec.PlaybackStateSpec

@Composable
fun AudioContent(blockItem: BlockItem.Audio, modifier: Modifier = Modifier) {
    MediaPlayer(
        source = blockItem.src,
        modifier = modifier,
    ) { playbackState, progressState, onPlayPause, onSeekTo ->
        PlayerContent(
            playbackState = playbackState,
            progressState = progressState,
            modifier = Modifier,
            onPlayPause = onPlayPause,
            onSeekTo = onSeekTo
        )
    }
}

@Composable
private fun PlayerContent(
    playbackState: PlaybackStateSpec,
    progressState: PlaybackProgressState,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
) {
    val textColor = Styler.textColor(null)

    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }

    val currentDuration = when (draggingProgress != null) {
        true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
        else -> progressState.currentDuration
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Styler.genericBackgroundColorForInteractiveBlock(),
            contentColor = textColor
        ),
        shape = Styler.roundedShape(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaybackPlayPause(
                spec = playbackState,
                contentColor = textColor,
                onPlayPause = onPlayPause,
            )

            Text(
                currentDuration,
                style = Styler.textStyle(null).copy(
                    fontFamily = Styler.defaultFontFamily(),
                    fontSize = 14.sp
                ),
                color = textColor
            )

            PlaybackSlider(
                isBuffering = playbackState.isBuffering,
                color = textColor,
                progressState = progressState,
                draggingProgress = draggingProgress,
                setDraggingProgress = setDraggingProgress,
                onSeekTo = onSeekTo,
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
@PreviewLightDark
private fun Preview() {
    BlocksPreviewTheme {
        Surface {
            PlayerContent(
                playbackState = PlaybackStateSpec.NONE.copy(
                    isPlayEnabled = true
                ),
                progressState = PlaybackProgressState(
                    total = 3 * 60 * 1000,
                    position = 60 * 1000
                ),
            )
        }
    }
}
