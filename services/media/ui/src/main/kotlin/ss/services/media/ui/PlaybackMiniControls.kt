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

package ss.services.media.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaMetadata
import app.ss.design.compose.extensions.isLargeScreen
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconSlot
import app.ss.design.compose.widget.icon.Icons
import ss.libraries.media.api.PLAYBACK_PROGRESS_INTERVAL
import ss.libraries.media.model.extensions.NONE_PLAYING
import ss.services.media.ui.common.Dismissible
import ss.services.media.ui.spec.NowPlayingSpec
import ss.services.media.ui.spec.PlaybackStateSpec
import ss.services.media.ui.spec.toSpec
import androidx.compose.material.icons.Icons as MaterialIcons
import app.ss.translations.R.string as RString
import ss.libraries.media.resources.R as MediaR

private object PlaybackMiniControlsDefaults {
    val height = 60.dp
    val maxWidth = 600.dp
    val playPauseSize = 30.dp
    val replaySize = 30.dp
    val cancelSize = 20.dp
}

@Composable
fun PlaybackMiniControls(
    playbackConnection: PlaybackConnection,
    modifier: Modifier = Modifier,
    onExpand: () -> Unit
) {
    val playbackState by playbackConnection.playbackState.collectAsStateWithLifecycle()
    val nowPlaying by playbackConnection.nowPlaying.collectAsStateWithLifecycle()

    val visible = (playbackState to nowPlaying).isActive

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it / 2 }),
        exit = slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        PlaybackMiniControls(
            spec = playbackState,
            nowPlayingSpec = nowPlaying.toSpec(),
            playbackConnection = playbackConnection,
            onExpand = onExpand
        )
    }
}

private inline val Pair<PlaybackStateSpec, MediaMetadata>.isActive
    get() = (first != PlaybackStateSpec.NONE && second != NONE_PLAYING) && first.canShowMini

@Composable
fun PlaybackMiniControls(
    spec: PlaybackStateSpec,
    nowPlayingSpec: NowPlayingSpec,
    modifier: Modifier = Modifier,
    height: Dp = PlaybackMiniControlsDefaults.height,
    playbackConnection: PlaybackConnection,
    onExpand: () -> Unit
) {
    val cancel: () -> Unit = { playbackConnection.releaseMini() }

    Dismissible(onDismiss = cancel) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = Color.Transparent,
                shape = MaterialTheme.shapes.medium,
                modifier = modifier
                    .padding(horizontal = Dimens.grid_5)
                    .padding(bottom = Dimens.grid_4)
                    .thenIf(isLargeScreen()) {
                        requiredSizeIn(
                            maxWidth = PlaybackMiniControlsDefaults.maxWidth
                        )
                    }
                    .align(Alignment.Center)
                    .clickable { onExpand() }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(playbackButtonSpacing()),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(height)
                            .fillMaxWidth()
                            .background(SsTheme.colors.playbackMiniBackground)
                    ) {
                        NowPlayingColumn(
                            spec = nowPlayingSpec,
                            onCancel = cancel
                        )
                        PlaybackReplay(
                            contentColor = SsTheme.colors.playbackMiniContent,
                            onRewind = {
                                playbackConnection.rewind()
                            }
                        )

                        PlaybackPlayPause(
                            spec = spec,
                            contentColor = SsTheme.colors.playbackMiniContent,
                            onPlayPause = {
                                playbackConnection.playPause()
                            }
                        )

                        Spacer(modifier = Modifier.width(Dimens.grid_2))
                    }
                    PlaybackProgress(
                        spec = spec,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        playbackConnection = playbackConnection
                    )
                }
            }
        }
    }
}

@Composable
private fun playbackButtonSpacing(
    isLargeScreen: Boolean = isLargeScreen()
): Dp = if (isLargeScreen) 12.dp else 8.dp

@Composable
private fun PlaybackProgress(
    spec: PlaybackStateSpec,
    color: Color,
    playbackConnection: PlaybackConnection
) {
    val progressState by playbackConnection.playbackProgress.collectAsStateWithLifecycle()
    val sizeModifier = Modifier
        .height(2.dp)
        .fillMaxWidth()

    val progress by animateFloatAsState(
        progressState.progress,
        tween(PLAYBACK_PROGRESS_INTERVAL.toInt(), easing = LinearEasing),
        label = "progress"
    )

    when {
        spec.isBuffering -> {
            LinearProgressIndicator(
                color = color,
                modifier = sizeModifier
            )
        }

        else -> {
            LinearProgressIndicator(
                progress = { progress },
                color = color,
                trackColor = color.copy(alpha = 0.24f),
                modifier = sizeModifier
            )
        }
    }
}

@Composable
private fun RowScope.NowPlayingColumn(
    spec: NowPlayingSpec,
    onCancel: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.grid_1),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
    ) {
        IconButton(onClick = onCancel) {
            IconBox(
                icon = Icons.Cancel,
                modifier = Modifier.size(PlaybackMiniControlsDefaults.cancelSize),
                contentColor = SsColors.BaseGrey2
            )
        }

        NowPlayingColumn(
            spec = spec,
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun NowPlayingColumn(
    spec: NowPlayingSpec,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            spec.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = SsTheme.typography.labelMedium.copy(
                fontSize = 15.sp
            ),
            color = SsTheme.colors.primaryForeground
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            spec.artist,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = SsTheme.typography.bodySmall,
            color = SsTheme.colors.secondaryForeground
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun PlaybackReplay(
    size: Dp = PlaybackMiniControlsDefaults.replaySize,
    contentColor: Color,
    onRewind: () -> Unit
) {
    IconButton(onClick = onRewind) {
        IconBox(
            icon = IconSlot.fromResource(
                MediaR.drawable.ic_audio_icon_backward,
                contentDescription = RString.ss_action_rewind
            ),
            modifier = Modifier.size(size),
            contentColor = contentColor
        )
    }
}

@Composable
fun PlaybackPlayPause(
    spec: PlaybackStateSpec,
    contentColor: Color,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    iconSize: Dp = PlaybackMiniControlsDefaults.playPauseSize
) {
    IconButton(
        onClick = onPlayPause,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    ) {
        if (spec.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(iconSize),
                color = contentColor,
            )
        } else {
            val painter = when {
                spec.isPlaying -> painterResource(id = MediaR.drawable.ic_audio_icon_pause)
                spec.isError -> rememberVectorPainter(MaterialIcons.Rounded.ErrorOutline)
                spec.isPlayEnabled -> painterResource(id = MediaR.drawable.ic_audio_icon_play)
                else -> rememberVectorPainter(MaterialIcons.Rounded.HourglassBottom)
            }

            IconBox(
                icon = IconSlot.fromPainter(
                    painter = painter,
                    contentDescription = stringResource(id = RString.ss_action_play_pause)
                ),
                modifier = Modifier.size(iconSize),
                contentColor = contentColor
            )
        }
    }
}
