package app.ss.media.playback.ui

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.media.playback.PLAYBACK_PROGRESS_INTERVAL
import app.ss.media.playback.PlaybackConnection
import app.ss.media.playback.extensions.NONE_PLAYBACK_STATE
import app.ss.media.playback.extensions.NONE_PLAYING
import app.ss.media.playback.extensions.isActive
import app.ss.media.playback.extensions.isBuffering
import app.ss.media.playback.extensions.isError
import app.ss.media.playback.extensions.isPlayEnabled
import app.ss.media.playback.extensions.isPlaying
import app.ss.media.playback.extensions.playPause
import app.ss.media.playback.model.AudioFile
import app.ss.media.playback.model.PlaybackProgressState
import app.ss.media.playback.model.toAudio
import app.ss.media.playback.ui.common.Dismissible
import app.ss.media.playback.ui.common.LocalPlaybackConnection
import app.ss.media.playback.ui.common.LocalPlaybackSheetState
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import com.cryart.design.ext.thenIf
import com.cryart.design.theme.BaseGrey1
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.BaseGrey3
import com.cryart.design.theme.Body
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.LabelMedium
import com.cryart.design.theme.SSTheme
import com.cryart.design.theme.isLargeScreen
import com.cryart.design.theme.lighter
import kotlinx.coroutines.launch

private object PlaybackMiniControlsDefaults {
    val height = 56.dp
    val maxWidth = 600.dp
    val playPauseSize = 42.dp
    val replaySize = 32.dp
    val cancelSize = 20.dp
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlaybackMiniControls(
    modifier: Modifier = Modifier,
    playbackConnection: PlaybackConnection
) {
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying).collectAsState(NONE_PLAYING)

    val visible = (playbackState to nowPlaying).isActive
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically({ it / 2 }),
        exit = slideOutVertically({ it / 2 })
    ) {
        PlaybackMiniControls(
            playbackState = playbackState,
            nowPlaying = nowPlaying,
            playbackConnection = playbackConnection,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlaybackMiniControls(
    playbackState: PlaybackStateCompat,
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    height: Dp = PlaybackMiniControlsDefaults.height,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    playbackSheetState: BottomSheetState = LocalPlaybackSheetState.current,
) {
    val coroutine = rememberCoroutineScope()
    val expand = { coroutine.launch { playbackSheetState.expand() } }
    val cancel: () -> Unit = { playbackConnection.transportControls?.stop() }

    val backgroundColor = playbackMiniBackgroundColor()
    val contentColor = playbackMiniContentColor()

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
                    .clickable { expand() }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(height)
                            .fillMaxWidth()
                            .background(backgroundColor)
                    ) {
                        NowPlayingColumn(
                            nowPlaying = nowPlaying,
                            onCancel = cancel
                        )
                        PlaybackReplay(
                            contentColor = contentColor,
                            onRewind = {
                                playbackConnection.transportControls?.rewind()
                            }
                        )
                        PlaybackPlayPause(
                            playbackState = playbackState,
                            contentColor = contentColor,
                            onPlayPause = {
                                playbackConnection.mediaController?.playPause()
                            }
                        )

                        Spacer(modifier = Modifier.width(Dimens.grid_2))
                    }
                    PlaybackProgress(
                        playbackState = playbackState,
                        color = playbackProgressColor(),
                        playbackConnection = playbackConnection
                    )
                }
            }
        }
    }
}

@Composable
private fun playbackMiniBackgroundColor(): Color =
    if (isSystemInDarkTheme()) {
        Color.Black.lighter()
    } else {
        BaseGrey1
    }

@Composable
private fun playbackMiniContentColor(): Color =
    if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }

@Composable
private fun playbackProgressColor(): Color =
    if (isSystemInDarkTheme()) {
        BaseGrey3
    } else {
        BaseGrey1
    }

@Composable
private fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    color: Color,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val progressState by rememberFlowWithLifecycle(playbackConnection.playbackProgress).collectAsState(PlaybackProgressState())
    val sizeModifier = Modifier
        .height(2.dp)
        .fillMaxWidth()
    when {
        playbackState.isBuffering -> {
            LinearProgressIndicator(
                color = color,
                modifier = sizeModifier
            )
        }
        else -> {
            LinearProgressIndicator(
                progress = animateFloatAsState(progressState.progress, tween(PLAYBACK_PROGRESS_INTERVAL.toInt(), easing = LinearEasing)).value,
                color = color,
                backgroundColor = color.copy(ProgressIndicatorDefaults.IndicatorBackgroundOpacity),
                modifier = sizeModifier
            )
        }
    }
}

@Composable
private fun RowScope.NowPlayingColumn(
    nowPlaying: MediaMetadataCompat,
    onCancel: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.grid_1),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
    ) {

        IconButton(
            onClick = onCancel
        ) {
            Icon(
                Icons.Rounded.Cancel,
                contentDescription = "Cancel",
                modifier = Modifier.size(PlaybackMiniControlsDefaults.cancelSize),
                tint = BaseGrey2
            )
        }

        NowPlayingColumn(
            audio = nowPlaying.toAudio(),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 2.dp),
        )
    }
}

@Composable
private fun NowPlayingColumn(
    audio: AudioFile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .padding(vertical = Dimens.grid_1)
            .fillMaxWidth()
            .then(modifier),
    ) {
        Text(
            audio.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = LabelMedium.copy(
                color = MaterialTheme.colors.onSurface
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                audio.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Body
            )
        }
    }
}

@Preview(
    name = "Now Playing Column"
)
@Composable
private fun NowPlayingColumnPreview() {
    SSTheme {
        Surface(color = playbackMiniBackgroundColor()) {
            NowPlayingColumn(
                audio = AudioFile(
                    "",
                    title = "Worn and Weary",
                    artist = "Adult Bible Study Guides"
                ),
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun PlaybackReplay(
    size: Dp = PlaybackMiniControlsDefaults.replaySize,
    contentColor: Color,
    onRewind: () -> Unit
) {
    IconButton(
        onClick = onRewind,
        modifier = Modifier.size(size)
    ) {
        Icon(
            Icons.Rounded.Replay10,
            contentDescription = "Rewind",
            tint = contentColor,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
private fun PlaybackPlayPause(
    playbackState: PlaybackStateCompat,
    size: Dp = PlaybackMiniControlsDefaults.playPauseSize,
    contentColor: Color,
    onPlayPause: () -> Unit
) {
    IconButton(
        onClick = onPlayPause,
        modifier = Modifier.size(size),
    ) {
        Icon(
            painter = rememberVectorPainter(
                when {
                    playbackState.isError -> Icons.Rounded.ErrorOutline
                    playbackState.isPlaying -> Icons.Rounded.Pause
                    playbackState.isPlayEnabled -> Icons.Rounded.PlayArrow
                    else -> Icons.Rounded.HourglassBottom
                }
            ),
            modifier = Modifier.size(size),
            contentDescription = "Play/Pause",
            tint = contentColor
        )
    }
}
