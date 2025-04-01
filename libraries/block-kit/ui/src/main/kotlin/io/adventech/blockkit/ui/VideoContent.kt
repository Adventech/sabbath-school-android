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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPlaybackSpeedState
import androidx.media3.ui.compose.state.rememberPresentationState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.media.MediaPlayer
import io.adventech.blockkit.ui.media.VideoSettingsDropdownMenu
import io.adventech.blockkit.ui.style.LatoFontFamily
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import io.adventech.blockkit.ui.style.thenIf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.extensions.millisToDuration
import ss.services.media.ui.PlaybackPlayPause
import ss.services.media.ui.common.PlaybackSlider
import ss.services.media.ui.spec.PlaybackSpeed
import ss.services.media.ui.spec.PlaybackStateSpec
import ss.services.media.ui.spec.SimpleTrack
import ss.services.media.ui.state.PlaybackSeekState
import ss.services.media.ui.state.rememberPlaybackCuesState
import ss.services.media.ui.state.rememberPlaybackSeekState
import ss.services.media.ui.state.rememberPlaybackTracksState
import ss.libraries.media.resources.R as MediaR

@Composable
fun VideoContent(blockItem: BlockItem.Video, modifier: Modifier = Modifier) {
    val textStyle = Styler.textStyle(null)

    MediaPlayer(
        source = blockItem.src,
        modifier = modifier,
    ) { exoplayer, playbackState, progressState, _, onSeekTo ->

        PlayerContent(
            exoPlayer = exoplayer,
            playbackState = playbackState,
            progressState = progressState,
            modifier = Modifier,
            onSeekTo = onSeekTo,
        )

        blockItem.caption?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                style = textStyle.copy(
                    fontStyle = FontStyle.Italic,
                    color = textStyle.color.copy(alpha = 0.7f),
                ),
                textAlign = TextAlign.Center
            )
        }

    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun PlayerContent(
    exoPlayer: ExoPlayer,
    playbackState: PlaybackStateSpec,
    progressState: PlaybackProgressState,
    modifier: Modifier = Modifier,
    onSeekTo: (Long) -> Unit = {},
) {
    val hazeState = remember { HazeState() }
    var isControlVisible by rememberSaveable { mutableStateOf(!playbackState.isPlaying) }
    var isMenuVisible by rememberSaveable { mutableStateOf(isControlVisible) }
    val playPauseButtonState = rememberPlayPauseButtonState(exoPlayer)
    val presentationState = rememberPresentationState(exoPlayer)
    val playbackSpeedState = rememberPlaybackSpeedState(exoPlayer)
    val playbackTracksState = rememberPlaybackTracksState(exoPlayer)
    val playbackCuesState = rememberPlaybackCuesState(exoPlayer)
    val playbackSeekState = rememberPlaybackSeekState(exoPlayer)
    val scaledModifier = Modifier.resizeWithContentScale(ContentScale.Fit, presentationState.videoSizeDp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(Styler.roundedShape()),
    ) {
        PlayerSurface(
            player = exoPlayer,
            modifier = scaledModifier
                .hazeSource(hazeState)
                .clickable { isControlVisible = !isControlVisible },
            surfaceType = SURFACE_TYPE_SURFACE_VIEW,
        )

        if (presentationState.coverSurface) {
            Box(Modifier
                .matchParentSize()
                .background(Color.Black))
        }

        VideoSubtitles(
            cues = playbackCuesState.cues,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        VideoControls(
            visible = isControlVisible,
            playbackState = playbackState,
            playbackSeekState = playbackSeekState,
            progressState = progressState,
            playbackSpeed = PlaybackSpeed.fromSpeed(playbackSpeedState.playbackSpeed),
            modifier = Modifier
                .fillMaxSize()
                .thenIf(isControlVisible) {
                    if (HazeDefaults.blurEnabled()) {
                        hazeEffect(hazeState, HazeMaterials.ultraThin(containerColor = Color.Black))
                    } else {
                        background(overlayColor, Styler.roundedShape())
                    }
                }
                .clip(Styler.roundedShape()),
            availableTracks = playbackTracksState.tracks,
            onPlayPause = { playPauseButtonState.onClick() },
            onSeekTo = onSeekTo,
            onPlaybackSpeedChange = { playbackSpeedState.updatePlaybackSpeed(it.speed) },
            onTrackSelected = { playbackTracksState.selectTrack(it) },
            onMenuShownChange = { isMenuVisible = it },
        )
    }

    LaunchedEffect(isControlVisible, isMenuVisible, playbackState) {
        if (isMenuVisible) {
            isControlVisible = true
        } else if (isControlVisible && playbackState.isPlaying) {
            delay(3000)
            isControlVisible = false
        } else {
            isControlVisible = !playbackState.isPlaying
        }
    }
}

@Composable
private fun VideoControls(
    visible: Boolean,
    playbackState: PlaybackStateSpec,
    playbackSeekState: PlaybackSeekState,
    progressState: PlaybackProgressState,
    playbackSpeed: PlaybackSpeed,
    modifier: Modifier = Modifier,
    availableTracks: ImmutableList<SimpleTrack> = persistentListOf(),
    onPlayPause: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onPlaybackSpeedChange: (PlaybackSpeed) -> Unit = {},
    onTrackSelected: (SimpleTrack?) -> Unit = {},
    onMenuShownChange: (Boolean) -> Unit = {},
) {
    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }
    val currentDuration = when (draggingProgress != null) {
        true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
        else -> progressState.currentDuration
    }
    val contentColor = Color.White
    var isMenuShown by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isMenuShown) { onMenuShownChange(isMenuShown) }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                MainPlayerControls(
                    playbackState = playbackState,
                    playbackSeekState = playbackSeekState,
                    modifier = Modifier.align(Alignment.Center),
                    contentColor = contentColor,
                    onPlayPause = onPlayPause,
                )
            }

            AnimatedVisibility(
                visible = visible,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessVeryLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    ),
                    targetOffsetY = { it }) + fadeOut(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = currentDuration,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontFamily = LatoFontFamily,
                    )

                    PlaybackSlider(
                        isBuffering = playbackState.isBuffering,
                        color = contentColor,
                        progressState = progressState,
                        draggingProgress = draggingProgress,
                        setDraggingProgress = setDraggingProgress,
                        onSeekTo = onSeekTo,
                        modifier = Modifier.weight(1f),
                    )

                    Text(
                        text = progressState.totalDuration,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontFamily = LatoFontFamily,
                    )

                    IconButton(
                        onClick = { isMenuShown = !isMenuShown },
                        modifier = Modifier,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            modifier = Modifier,
                            tint = contentColor
                        )
                    }
                }
            }
        }
    }

    VideoSettingsDropdownMenu(
        isMenuShown = isMenuShown,
        onDismissRequest = { isMenuShown = false },
        availableTracks = availableTracks,
        playbackSpeed = playbackSpeed,
        modifier = Modifier,
        onPlaybackSpeedChange = {
            onPlaybackSpeedChange(it)
            isMenuShown = false
        },
        onTrackSelected = {
            onTrackSelected(it)
            isMenuShown = false
        },
    )
}

@Composable
private fun MainPlayerControls(
    playbackState: PlaybackStateSpec,
    playbackSeekState: PlaybackSeekState,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
    onPlayPause: () -> Unit = {},
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (playbackSeekState.isReplayEnabled) {
            IconButton(
                onClick = { playbackSeekState.seekBack() },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(48.dp),
            ) {
                Icon(
                    painter = painterResource(MediaR.drawable.ic_audio_icon_backward),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = contentColor
                )
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }

        PlaybackPlayPause(
            spec = playbackState,
            contentColor = contentColor,
            onPlayPause = onPlayPause,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(64.dp),
            iconSize = 48.dp,
        )

        if (playbackSeekState.isForwardEnabled) {
            IconButton(
                onClick = { playbackSeekState.seekForward() },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(48.dp),
            ) {
                Icon(
                    painter = painterResource(MediaR.drawable.ic_audio_icon_forward),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = contentColor
                )
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Composable
fun VideoSubtitles(cues: ImmutableList<Cue>, modifier: Modifier) {
    val text = remember(cues) {
        buildString {
            cues.forEachIndexed { index, cue ->
                append(cue.text)
                if (cues.lastIndex != index) {
                    append("\n")
                }
            }
        }
    }
    if (text.isNotEmpty()) {
        Text(
            text = text,
            modifier = modifier
                .padding(6.dp)
                .background(overlayColor, RoundedCornerShape(6.dp))
                .padding(4.dp),
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = LatoFontFamily,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

private val overlayColor = Color.Black.copy(alpha = 0.38f)

@Preview
@Composable
private fun Preview() {
    BlocksPreviewTheme {
        Surface(color = Color.Black) {
            VideoControls(
                visible = true,
                playbackState = PlaybackStateSpec.NONE.copy(isPlaying = true),
                playbackSeekState = object: PlaybackSeekState {
                    override val isForwardEnabled: Boolean = true
                    override val isReplayEnabled: Boolean = true
                    override fun seekForward() {}
                    override fun seekBack() {}
                },
                progressState = PlaybackProgressState(
                    total = 10000L,
                    position = 3000L,
                ),
                playbackSpeed = PlaybackSpeed.NORMAL,
                onPlayPause = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(Styler.roundedShape()),
            )
        }
    }
}


@androidx.annotation.OptIn(UnstableApi::class)
@Preview
@Composable
private fun PreviewSubtitles() {
    val previewCue = remember {
        Cue.Builder()
            .setText("May the Lord be able to say of every child, every young person, and every parent")
            .build()
    }

    BlocksPreviewTheme {
        Surface(color = Color.White) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
            ) {
                VideoSubtitles(
                    cues = persistentListOf(previewCue),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}
