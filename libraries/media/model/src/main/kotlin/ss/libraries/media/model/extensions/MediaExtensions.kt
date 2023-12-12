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

package ss.libraries.media.model.extensions

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import ss.libraries.media.model.NowPlaying
import java.util.concurrent.TimeUnit

const val KEY_DURATION = "media:key_duration"
const val KEY_ID = "media:key_id"
const val KEY_SOURCE = "media:key_source"
const val KEY_TARGET = "media:target"
const val KEY_TARGET_INDEX = "media:target_index"

val NONE_PLAYING: MediaMetadata = MediaMetadata.EMPTY

fun MediaMetadata.toNowPlaying() = NowPlaying(
    id = id,
    title = title.toString(),
    artist = artist.toString(),
    artworkUri = artworkUri,
)

inline val MediaMetadata.duration: Long get() = extras?.getLong(KEY_DURATION) ?: 0L
inline val MediaMetadata.id: String get() = extras?.getString(KEY_ID, "") ?: ""
inline val MediaMetadata.source: Uri get() = (extras?.getString(KEY_SOURCE, "") ?: "").toUri()
inline val MediaMetadata.target: String? get() = (extras?.getString(KEY_TARGET))
inline val MediaMetadata.targetIndex: String? get() = (extras?.getString(KEY_TARGET_INDEX))

fun Long.millisToDuration(): String {
    val seconds = (TimeUnit.SECONDS.convert(this, TimeUnit.MILLISECONDS) % 60).toInt()
    val minutes = (TimeUnit.MINUTES.convert(this, TimeUnit.MILLISECONDS) % 60).toInt()
    val hours = (TimeUnit.HOURS.convert(this, TimeUnit.MILLISECONDS) % 24).toInt()

    "${timeAddZeros(hours)}:${timeAddZeros(minutes, "0")}:${timeAddZeros(seconds, "00")}".apply {
        return if (startsWith(":")) replaceFirst(":", "") else this
    }
}

private fun timeAddZeros(number: Int?, ifZero: String = ""): String {
    return when (number) {
        0 -> ifZero
        else -> number?.toString()?.padStart(2, '0') ?: "00"
    }
}
