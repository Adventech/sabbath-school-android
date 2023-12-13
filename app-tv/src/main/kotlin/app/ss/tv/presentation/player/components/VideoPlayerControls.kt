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


package app.ss.tv.presentation.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.ss.tv.presentation.theme.SSTvTheme
import ss.libraries.media.model.PlaybackProgressState

@Immutable
data class VideoPlayerControlsSpec(
    val progressState: PlaybackProgressState,
    val title: String,
    val artist: String,
)

@Composable
fun VideoPlayerControls(
    spec: VideoPlayerControlsSpec,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                )
            )
            .padding(horizontal = 56.dp)
            .padding(
                bottom = 32.dp,
                top = 64.dp,
            )
    ) {

        Text(
            text = spec.title,
            style = MaterialTheme.typography.headlineMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )

        Text(
            text = spec.artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            fontWeight = FontWeight.Light,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        Row(
            modifier = Modifier
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            val progressState = spec.progressState
            DurationText(text = progressState.currentDuration)

            val onSeekProgress: (Float) -> Unit = remember {
                { seekProgress -> onSeek(progressState.total.times(seekProgress).toLong()) }
            }

            VideoPlayerControllerIndicator(
                progress = progressState.progress,
                isFocused = isFocused,
                onSeek = onSeekProgress,
                modifier = Modifier.weight(1f)
            )

            DurationText(text = progressState.totalDuration)
        }
    }
}

@Composable
private fun DurationText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
@Preview
private fun Preview() {
    SSTvTheme {
        VideoPlayerControls(
            spec = VideoPlayerControlsSpec(
                progressState = PlaybackProgressState(
                    elapsed = 5000,
                    total = 85000,
                ),
                title = "Worshiping the Creator",
                artist = "Hope Sabbath School"
            ),
            onSeek = {}
        )
    }
}
