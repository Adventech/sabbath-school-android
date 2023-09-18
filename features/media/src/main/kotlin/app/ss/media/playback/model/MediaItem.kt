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

package app.ss.media.playback.model

import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import app.ss.media.playback.extensions.KEY_DURATION
import app.ss.media.playback.extensions.KEY_ID
import app.ss.media.playback.extensions.KEY_SOURCE
import app.ss.media.playback.extensions.duration
import app.ss.media.playback.extensions.id
import app.ss.media.playback.extensions.source
import app.ss.models.media.AudioFile

internal fun MediaMetadata.toAudio(): AudioFile = AudioFile(
    id = id,
    title = "$title",
    artist = "$artist",
    source = source,
    duration = duration,
    image = artworkUri.toString()
)

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal fun AudioFile.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id)
    .setUri(source)
    .setMimeType(MimeTypes.BASE_TYPE_AUDIO)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setDescription(artist)
            .setArtworkUri(image.toUri())
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .setExtras(
                bundleOf(
                    KEY_ID to id,
                    KEY_DURATION to duration,
                    KEY_SOURCE to source.toString()
                )
            )
            .build()
    )
    .build()
