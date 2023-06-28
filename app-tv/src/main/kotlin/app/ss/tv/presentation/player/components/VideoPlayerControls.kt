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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.ss.tv.presentation.player.VideoPlayerState
import app.ss.tv.presentation.player.rememberVideoPlayerState
import app.ss.tv.presentation.theme.SSTvTheme
import app.ss.translations.R.string as RString

@Immutable
data class VideoPlayerControlsSpec(
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    val onPlayPauseToggle: () -> Unit,
    val onSeek: (Float) -> Unit,
    val contentProgressInMillis: Long,
    val contentDurationInMillis: Long,
    val title: String,
    val artist: String,
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VideoPlayerControls(
    spec: VideoPlayerControlsSpec,
    videoPlayerState: VideoPlayerState,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    val contentProgress by remember(spec.contentProgressInMillis, spec.contentDurationInMillis) {
        derivedStateOf {
            spec.contentProgressInMillis.toFloat() / spec.contentDurationInMillis
        }
    }

    val contentProgressString by remember(spec.contentProgressInMillis) {
        derivedStateOf { buildProgressString(spec.contentProgressInMillis) }
    }

    val contentDurationString by remember(spec.contentDurationInMillis) {
        derivedStateOf { buildDurationString(spec.contentDurationInMillis) }
    }

    LaunchedEffect(videoPlayerState.isDisplayed) {
        if (videoPlayerState.isDisplayed) {
            focusRequester.requestFocus()
        }
    }

    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = videoPlayerState.isDisplayed,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
                .padding(horizontal = 56.dp)
                .padding(
                    bottom = 32.dp,
                    top = 64.dp
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
                modifier = Modifier.padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                AnimatedContent(targetState = spec.isBuffering) { isBuffering ->
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .size(ControlsIconSize)
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        VideoPlayerControlsIcon(
                            isPlaying = spec.isPlaying,
                            contentDescription = stringResource(id = RString.ss_action_play_pause),
                            modifier = Modifier.focusRequester(focusRequester),
                            onClick = { spec.onPlayPauseToggle() }
                        )
                    }
                }

                DurationText(text = contentProgressString)

                VideoPlayerControllerIndicator(
                    progress = contentProgress,
                    videoPlayerState = videoPlayerState,
                    onSeek = spec.onSeek
                )

                DurationText(text = contentDurationString)
            }
        }
    }
}

private fun buildProgressString(contentProgressInMillis: Long): String {
    val contentProgressMinutes = (contentProgressInMillis / 1000) / 60
    val contentProgressSeconds = (contentProgressInMillis / 1000) % 60
    val contentProgressMinutesStr =
        if (contentProgressMinutes < 10) {
            contentProgressMinutes.padStartWith0()
        } else {
            contentProgressMinutes.toString()
        }
    val contentProgressSecondsStr =
        if (contentProgressSeconds < 10) {
            contentProgressSeconds.padStartWith0()
        } else {
            contentProgressSeconds.toString()
        }
    return "$contentProgressMinutesStr:$contentProgressSecondsStr"
}

private fun buildDurationString(contentDurationInMillis: Long): String {
    val contentDurationMinutes =
        (contentDurationInMillis / 1000 / 60).coerceAtLeast(minimumValue = 0)
    val contentDurationSeconds =
        (contentDurationInMillis / 1000 % 60).coerceAtLeast(minimumValue = 0)
    val contentDurationMinutesStr =
        if (contentDurationMinutes < 10) {
            contentDurationMinutes.padStartWith0()
        } else {
            contentDurationMinutes.toString()
        }
    val contentDurationSecondsStr =
        if (contentDurationSeconds < 10) {
            contentDurationSeconds.padStartWith0()
        } else {
            contentDurationSeconds.toString()
        }
    return "$contentDurationMinutesStr:$contentDurationSecondsStr"
}

private fun Long.padStartWith0() = this.toString().padStart(2, '0')

@Composable
private fun DurationText(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 12.dp),
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
            VideoPlayerControlsSpec(
                isPlaying = true,
                isBuffering = false,
                onPlayPauseToggle = {},
                onSeek = {},
                contentProgressInMillis = 5000,
                contentDurationInMillis = 85000,
                title = "Worshiping the Creator",
                artist = "Hope Sabbath School"
            ),
            videoPlayerState = rememberVideoPlayerState()
        )
    }
}
