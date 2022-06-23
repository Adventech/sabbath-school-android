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

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.Spacing16
import app.ss.design.compose.theme.Spacing4
import app.ss.design.compose.theme.Spacing6
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.dividerColor
import app.ss.design.compose.theme.onSurfaceSecondary
import app.ss.media.playback.ui.spec.NowPlayingSpec
import app.ss.media.playback.ui.spec.PlaybackQueueSpec
import app.ss.media.playback.ui.spec.toSpec
import kotlinx.coroutines.launch

private const val scrollToItemKey = "playbackQueue"

@Composable
internal fun PlaybackQueueList(
    spec: PlaybackQueueSpec,
    modifier: Modifier = Modifier,
) {
    val coroutine = rememberCoroutineScope()
    val (playbackQueue, listState, nowPlayingId, isPlaying, onPlayAudio) = spec

    LazyColumn(
        modifier = modifier
            .padding(vertical = Spacing16),
        state = listState,
    ) {
        itemsIndexed(
            playbackQueue,
            key = { _: Int, item: NowPlayingSpec -> item.id }
        ) { index, audio ->
            AudioRow(
                spec = audio,
                isSelected = audio.id == nowPlayingId,
                isPlaying = isPlaying,
                onClick = {
                    onPlayAudio(index)
                }
            )
            Divider(
                color = dividerColor(),
                thickness = 0.5.dp
            )
        }
    }

    if (playbackQueue.isEmpty()) return

    val position = playbackQueue.indexOfFirst { it.id == nowPlayingId }
    if (position > 0) {
        LaunchedEffect(key1 = scrollToItemKey) {
            coroutine.launch {
                listState.scrollToItem(position)
            }
        }
    }
}

@Composable
private fun AudioRow(
    spec: NowPlayingSpec,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing4)
            .selectable(selected = isSelected, onClick = onClick)
    ) {

        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing4)
        ) {
            Spacer(modifier = Modifier.height(Spacing6))
            Text(
                text = spec.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = spec.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp
                ),
                color = onSurfaceSecondary(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(Spacing6))
        }

        if (isSelected && isPlaying) {
            NowPlayingAnimation()
        }
    }
}

@Preview(
    name = "Queue"
)
@Preview(
    name = "Queue ~ Dark",
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun AudioRowPreview() {
    SsTheme {
        Surface {
            AudioRow(
                spec = sampleAudio.toSpec(),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(
    name = "Queue ~ Playing"
)
@Preview(
    name = "Queue ~ Playing ~ Dark",
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun AudioRowPreviewPlaying() {
    SsTheme {
        Surface {
            AudioRow(
                spec = sampleAudio.toSpec(),
                modifier = Modifier.padding(8.dp),
                isPlaying = true,
                isSelected = true
            )
        }
    }
}

@Composable
private fun NowPlayingAnimation() {
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        Modifier.size(24.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        infiniteTransition.PulsingLine(
            2,
            duration = 450
        )
        infiniteTransition.PulsingLine(
            3,
            duration = 500
        )
        infiniteTransition.PulsingLine(
            2,
            duration = 600
        )
    }
}

@Composable
fun InfiniteTransition.PulsingLine(
    heightFactor: Int,
    duration: Int
) {
    val scale by animateFloat(
        1f,
        heightFactor.toFloat(),
        infiniteRepeatable(tween(duration), RepeatMode.Reverse)
    )

    Box(
        Modifier
            .size(
                width = 4.dp,
                height = (8 * scale).dp
            )
            .background(
                MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50)
            )
    )
}
