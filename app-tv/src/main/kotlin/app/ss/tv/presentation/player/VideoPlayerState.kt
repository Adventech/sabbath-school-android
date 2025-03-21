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

package app.ss.tv.presentation.player

import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Immutable
class VideoPlayerState internal constructor(
    @param:IntRange(from = 0) val hideSeconds: Int,
    private val scope: CoroutineScope
) {
    var isDisplayed by mutableStateOf(true)

    private var delayedHandler: (() -> Unit)? = null

    /**
     * Show player controls and they will auto-hide after [hideSeconds] unless [indefinite] is true.
     *
     * @param indefinite If `true` controls will not auto-hide.
     */
    fun showControls(indefinite: Boolean = false) {
        isDisplayed = true
        if (indefinite) {
            delayedHandler = null
            return
        }

        delayedHandler = { isDisplayed = false }
        scope.launch {
            delay(hideSeconds.seconds)
            delayedHandler?.invoke()
        }
    }
}

@Composable
fun rememberVideoPlayerState(
    @IntRange(from = 0) hideSeconds: Int = 5,
    scope: CoroutineScope = rememberCoroutineScope()
) = remember {
    VideoPlayerState(
        hideSeconds = hideSeconds,
        scope = scope
    )
}
