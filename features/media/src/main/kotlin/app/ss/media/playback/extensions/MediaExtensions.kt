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

package app.ss.media.playback.extensions

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import app.ss.media.playback.ui.spec.PlaybackStateSpec

internal const val KEY_DURATION = "media:key_duration"
internal const val KEY_ID = "media:key_id"
internal const val KEY_SOURCE = "media:key_source"

internal val NONE_PLAYBACK_STATE: PlaybackStateSpec = PlaybackStateSpec.NONE

val NONE_PLAYING: MediaMetadata = MediaMetadata.EMPTY

internal inline val MediaMetadata.duration: Long get() = extras?.getLong(KEY_DURATION) ?: 0L
internal inline val MediaMetadata.id: String get() = extras?.getString(KEY_ID, "") ?: ""
internal inline val MediaMetadata.source: Uri get() = (extras?.getString(KEY_SOURCE, "") ?: "").toUri()
