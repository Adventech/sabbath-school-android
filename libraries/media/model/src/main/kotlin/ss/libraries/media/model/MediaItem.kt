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

package ss.libraries.media.model

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import app.ss.models.media.AudioFile
import app.ss.models.media.SSVideo
import ss.libraries.media.model.extensions.KEY_DURATION
import ss.libraries.media.model.extensions.KEY_ID
import ss.libraries.media.model.extensions.KEY_SOURCE
import ss.libraries.media.model.extensions.KEY_TARGET
import ss.libraries.media.model.extensions.KEY_TARGET_INDEX
import ss.libraries.media.model.extensions.duration
import ss.libraries.media.model.extensions.id
import ss.libraries.media.model.extensions.source
import ss.libraries.media.model.extensions.target
import ss.libraries.media.model.extensions.targetIndex

fun MediaMetadata.toAudio(): AudioFile = AudioFile(
    id = id,
    title = "${title ?: ""}",
    artist = "${artist ?: ""}",
    source = source,
    duration = duration,
    image = artworkUri.toString(),
)

fun MediaMetadata.toVideo(): SSVideo = SSVideo(
    id = id,
    title = "${title ?: ""}",
    artist = "${artist ?: ""}",
    src = source.toString(),
    thumbnail = artworkUri.toString(),
    targetIndex = targetIndex ?: "",
    target = target ?: "",
)

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun AudioFile.toMediaItem(): MediaItem = MediaItem.Builder()
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
                Bundle().apply {
                    putString(KEY_ID, id)
                    putLong(KEY_DURATION, duration)
                    putString(KEY_SOURCE, source.toString())
                    putString(KEY_TARGET, target)
                    putString(KEY_TARGET_INDEX, targetIndex)
                }
            )
            .build()
    )
    .build()

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun SSVideo.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id)
    .setUri(hls ?: src)
    .setMimeType(hls?.let { MimeTypes.APPLICATION_M3U8 } ?: MimeTypes.BASE_TYPE_VIDEO)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setDescription(artist)
            .setArtworkUri(thumbnail.toUri())
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .setExtras(
                Bundle().apply {
                    putString(KEY_ID, id)
                    putString(KEY_SOURCE, (hls ?: src))
                    putString(KEY_TARGET, target)
                    putString(KEY_TARGET_INDEX, targetIndex)
                }
            )
            .build()
    )
    .build()
