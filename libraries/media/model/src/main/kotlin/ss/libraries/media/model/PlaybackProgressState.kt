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

import androidx.compose.runtime.Immutable
import ss.libraries.media.model.extensions.millisToDuration

@Immutable
data class PlaybackProgressState(
    val total: Long = 0L,
    val position: Long = 0L,
    val elapsed: Long = 0L,
    val buffered: Long = 0L
) {

    val progress get() = ((position.toFloat() + elapsed) / (total + 1).toFloat()).coerceIn(0f, 1f)
    val bufferedProgress get() = ((buffered.toFloat()) / (total + 1).toFloat()).coerceIn(0f, 1f)

    val currentDuration get() = (position + elapsed).millisToDuration()
    val totalDuration get() = total.millisToDuration()
}
