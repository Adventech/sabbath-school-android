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

package app.ss.media.playback.model

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import app.ss.media.playback.extensions.UNTITLED
import app.ss.media.playback.extensions.artist
import app.ss.media.playback.extensions.artworkUri
import app.ss.media.playback.extensions.duration
import app.ss.media.playback.extensions.id
import app.ss.media.playback.extensions.source
import app.ss.media.playback.extensions.title

fun List<MediaSessionCompat.QueueItem>?.toMediaIdList(): List<MediaId> {
    return this?.map { it.description.mediaId?.toMediaId() ?: MediaId() } ?: emptyList()
}

fun AudioFile.toMediaMetadata(builder: MediaMetadataCompat.Builder): MediaMetadataCompat.Builder = builder.apply {
    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, artist)
    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
    putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, MediaId(MEDIA_TYPE_AUDIO, id).toString())
    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
    putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, image)
    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, source.toString())
}

fun MediaMetadataCompat.toAudio(): AudioFile = AudioFile(
    id = id,
    title = title ?: UNTITLED,
    artist = artist ?: UNTITLED,
    source = source,
    duration = duration,
    image = artworkUri.toString()
)
