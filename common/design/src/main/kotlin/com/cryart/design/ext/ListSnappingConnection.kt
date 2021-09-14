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

package com.cryart.design.ext

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Velocity

/**
 * Snaps a [androidx.compose.foundation.lazy.LazyRow] after a fling.
 *
 * Snap position will be [LazyListState.firstVisibleItemIndex] or
 * [LazyListState.firstVisibleItemIndex] + 1 if the firstVisibleItemIndex
 * has scrolled more than halfway through.
 *
 * @param listState : Added to the [[androidx.compose.foundation.lazy.LazyRow]]
 * @param widthState : This should be a [State] providing the width (in pixels) of
 * the item
 */
class ListSnappingConnection(
    private val listState: LazyListState,
    private val widthState: State<Int>,
) : NestedScrollConnection {

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val position = listState.firstVisibleItemIndex
        val offset = listState.firstVisibleItemScrollOffset
        val lastIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

        if (offset > 0) {
            val index = when {
                offset <= widthState.value / 2 -> position
                else -> position + 1
            }
            listState.animateScrollToItem(index.coerceAtMost(lastIndex))
        }
        return super.onPostFling(consumed, available)
    }
}
