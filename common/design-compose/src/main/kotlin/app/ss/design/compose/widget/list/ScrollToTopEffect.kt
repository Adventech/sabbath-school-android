/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

package app.ss.design.compose.widget.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Listens to the [LocalScrollToTop] flow and scrolls the provided [LazyListState]
 * to the specified position when the event is emitted.
 *
 * @param state The LazyListState to control.
 * @param targetIndex The item index to scroll to (default 0).
 * @param targetOffset The offset in pixels from the start of the item (default 0).
 */
@Composable
fun ScrollToTopEffect(
    state: LazyListState,
    targetIndex: Int = 0,
    targetOffset: Int = 0
) {
    val scrollToTopFlow = LocalScrollToTop.current

    LaunchedEffect(scrollToTopFlow, state, targetIndex, targetOffset) {
        scrollToTopFlow.collect {
            state.animateScrollToItem(targetIndex, targetOffset)
        }
    }
}

val LocalScrollToTop = staticCompositionLocalOf<Flow<Unit>> { emptyFlow() }
