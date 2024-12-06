/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package ss.feed.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.ss.models.feed.FeedView
import ss.feed.components.view.FeedResourceView
import ss.feed.model.FeedResourceSpec

@Composable
internal fun FeedResource(
    spec: FeedResourceSpec,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val coverSpec by remember(spec) {
        mutableStateOf(
            FeedResourceCoverSpec(
                title = spec.title,
                type = when (spec.view) {
                    FeedView.UNKNOWN,
                    FeedView.TILE,
                    FeedView.BANNER -> ResourceCoverType.LANDSCAPE
                    FeedView.SQUARE -> ResourceCoverType.SQUARE
                    FeedView.FOLIO -> ResourceCoverType.PORTRAIT
                },
                direction = spec.direction,
                covers = spec.covers,
                view = spec.view,
                primaryColor = spec.primaryColor
            )
        )
    }

    FeedResourceView(spec.title, coverSpec, modifier, onClick)
}

