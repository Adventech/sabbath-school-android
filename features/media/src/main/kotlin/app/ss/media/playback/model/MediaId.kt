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

import timber.log.Timber

const val MEDIA_TYPE_AUDIO = "Media.Audio"
const val MEDIA_TYPE_ARTIST = "Media.Artist"
const val MEDIA_TYPE_ALBUM = "Media.Album"
const val MEDIA_TYPE_AUDIO_QUERY = "Media.AudioQuery"
const val MEDIA_TYPE_AUDIO_MINERVA_QUERY = "Media.AudioMinervaQuery"
const val MEDIA_TYPE_AUDIO_FLACS_QUERY = "Media.AudioFlacsQuery"

private const val MEDIA_ID_SEPARATOR = " | "

data class MediaId(
    val type: String = MEDIA_TYPE_AUDIO,
    val value: String = "0",
    val index: Int = 0,
    val caller: String = CALLER_SELF
) {

    companion object {
        const val CALLER_SELF = "self"
        const val CALLER_OTHER = "other"
    }

    override fun toString(): String {
        return type +
            MEDIA_ID_SEPARATOR + value +
            MEDIA_ID_SEPARATOR + index +
            MEDIA_ID_SEPARATOR + caller
    }
}

fun String.toMediaId(): MediaId {
    val parts = split(MEDIA_ID_SEPARATOR)
    val type = parts[0]

    val knownTypes = listOf(
        MEDIA_TYPE_AUDIO,
        MEDIA_TYPE_ARTIST,
        MEDIA_TYPE_ALBUM,
        MEDIA_TYPE_AUDIO_QUERY,
        MEDIA_TYPE_AUDIO_MINERVA_QUERY,
        MEDIA_TYPE_AUDIO_FLACS_QUERY
    )
    if (type !in knownTypes) {
        Timber.e("Unknown media type: $type")
        return MediaId()
    }

    return if (parts.size > 1) {
        MediaId(type, parts[1], parts[2].toInt(), parts[3])
    } else MediaId()
}
