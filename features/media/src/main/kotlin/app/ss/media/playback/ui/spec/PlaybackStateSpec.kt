/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.media.playback.ui.spec

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.Immutable
import app.ss.media.playback.extensions.isBuffering
import app.ss.media.playback.extensions.isError
import app.ss.media.playback.extensions.isPlayEnabled
import app.ss.media.playback.extensions.isPlaying

@Immutable
data class PlaybackStateSpec(
    val isPlaying: Boolean,
    val isPlayEnabled: Boolean,
    val isError: Boolean,
    val isBuffering: Boolean,
)

internal fun PlaybackStateCompat.toSpec() = PlaybackStateSpec(
    isPlaying, isPlayEnabled, isError, isBuffering
)
