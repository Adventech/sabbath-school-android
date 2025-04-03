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

package io.adventech.blockkit.ui.media

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.CascadeDropdownMenuItem
import me.saket.cascade.rememberCascadeState
import ss.services.media.ui.spec.PlaybackSpeed
import ss.services.media.ui.spec.SimpleTrack
import app.ss.translations.R as L10nR
import io.adventech.blockkit.ui.R as BlockKitR

@Composable
internal fun VideoSettingsDropdownMenu(
    isMenuShown: Boolean,
    onDismissRequest: () -> Unit,
    availableTracks: ImmutableList<SimpleTrack>,
    playbackSpeed: PlaybackSpeed,
    modifier: Modifier = Modifier,
    onPlaybackSpeedChange: (PlaybackSpeed) -> Unit = {},
    onTrackSelected: (SimpleTrack?) -> Unit = {},
) {
    val cascadeState = rememberCascadeState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    CascadeDropdownMenu(
        expanded = isMenuShown,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        state = cascadeState,
        offset = DpOffset(screenWidth, 0.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        CascadeDropdownMenuItem(
            text = { ParentMenuItem(L10nR.string.ss_media_playback_speed, Icons.Rounded.SlowMotionVideo) },
            children = {
                PlaybackSpeed.entries.forEach { speed ->
                    DropdownMenuItem(
                        text = { ChildMenuItem(speed.label, playbackSpeed == speed) },
                        onClick = { onPlaybackSpeedChange(speed) },
                    )
                }
            },
            cascadeState = cascadeState,
        )
        availableTracks.filter { it is SimpleTrack.Audio }.takeUnless { it.isEmpty() }?.let { tracks ->
            CascadeDropdownMenuItem(
                text = { ParentMenuItem(L10nR.string.ss_media_audio, BlockKitR.drawable.ic_voice_selection) },
                children = {
                    tracks.forEach { track ->
                        DropdownMenuItem(
                            text = { ChildMenuItem(track.label, track.isSelected) },
                            onClick = { onTrackSelected(track) },
                        )
                    }
                },
                cascadeState = cascadeState
            )
        }

        availableTracks.filter { it is SimpleTrack.Subtitle }.takeUnless { it.isEmpty() }?.let { tracks ->
            CascadeDropdownMenuItem(
                text = { ParentMenuItem(L10nR.string.ss_media_captions, Icons.Rounded.ClosedCaption) },
                children = {
                    DropdownMenuItem(
                        text = { ChildMenuItem(stringResource(L10nR.string.ss_off), tracks.none { it.isSelected }) },
                        onClick = { onTrackSelected(null) },
                    )

                    tracks.forEach { track ->
                        DropdownMenuItem(
                            text = { ChildMenuItem(track.label, track.isSelected) },
                            onClick = { onTrackSelected(track) },
                        )
                    }
                },
                cascadeState = cascadeState
            )
        }
    }
}

@Composable
private fun ParentMenuItem(
    @StringRes text: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier,
        )

        Text(
            text = stringResource(text),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ParentMenuItem(
    @StringRes text: Int,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier,
        )

        Text(
            text = stringResource(text),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChildMenuItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier,
            )
        }
    }
}
