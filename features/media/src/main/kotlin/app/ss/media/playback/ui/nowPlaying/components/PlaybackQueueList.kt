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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.media.playback.model.AudioFile
import app.ss.media.playback.model.PlaybackQueue
import com.cryart.design.theme.Body
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.SSTheme
import com.cryart.design.theme.Spacing4
import com.cryart.design.theme.Spacing6
import com.cryart.design.theme.Spacing8
import com.cryart.design.theme.TitleSmall

@Composable
internal fun PlaybackQueueList(
    playbackQueue: PlaybackQueue,
    modifier: Modifier = Modifier,
    nowPlayingId: String? = null,
    onPlayAudio: (Int) -> Unit,
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            vertical = Spacing8,
            horizontal = Dimens.grid_4
        )
    ) {
        itemsIndexed(playbackQueue.audiosList) { index, audio ->
            AudioRow(
                audio = audio,
                isPlaying = audio.id == nowPlayingId,
                onClick = {
                    onPlayAudio(index)
                }
            )
            Divider()
        }
    }
}

@Composable
private fun AudioRow(
    audio: AudioFile,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing4)
            .clickable {
                if (isPlaying.not()) {
                    onClick()
                }
            }
    ) {

        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing4)
        ) {
            Spacer(modifier = Modifier.height(Spacing6))
            Text(
                text = audio.title,
                style = TitleSmall.copy(
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = if (isPlaying) FontWeight.Black else FontWeight.Medium,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = audio.artist,
                    style = Body.copy(
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(Spacing6))
        }
    }
}

@Preview(
    name = "Queue"
)
@Composable
private fun AudioRowPreview() {
    SSTheme {
        Surface {
            AudioRow(
                audio = sampleAudio,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(
    name = "Queue ~ Playing"
)
@Composable
private fun AudioRowPreviewPlaying() {
    SSTheme {
        Surface {
            AudioRow(
                audio = sampleAudio,
                modifier = Modifier.padding(8.dp),
                isPlaying = true
            )
        }
    }
}
