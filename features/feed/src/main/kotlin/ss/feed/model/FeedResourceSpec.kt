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

package ss.feed.model

import androidx.compose.runtime.Immutable
import app.ss.models.feed.FeedDirection
import app.ss.models.feed.FeedGroup
import app.ss.models.feed.FeedResource
import app.ss.models.feed.FeedView
import ss.feed.components.FeedResourceCoverSpec
import ss.feed.components.ResourceCoverType
import ss.feed.model.FeedResourceSpec.ContentDirection

@Immutable
data class FeedResourceSpec(
    val id: String,
    val name: String,
    val title: String,
    val subtitle: String?,
    val direction: ContentDirection,
    val coverSpec: FeedResourceCoverSpec,
) {
    /** Direction of the Image, title and subtitle, Not to be confused with [FeedDirection]. */
    enum class ContentDirection {
        VERTICAL,
        HORIZONTAL
    }
}

internal fun FeedResource.toSpec(
    group: FeedGroup,
    direction: ContentDirection = ContentDirection.VERTICAL,
): FeedResourceSpec {
    return FeedResourceSpec(
        id = id,
        name = name,
        title = title,
        subtitle = subtitle,
        direction = direction,
        coverSpec = FeedResourceCoverSpec(
            title = title,
            type = when (group.view) {
                FeedView.UNKNOWN,
                FeedView.TILE,
                FeedView.BANNER -> ResourceCoverType.LANDSCAPE
                FeedView.SQUARE -> ResourceCoverType.SQUARE
                FeedView.FOLIO -> ResourceCoverType.PORTRAIT
            },
            direction = group.direction,
            covers = covers,
            view = group.view,
            primaryColor = primaryColor
        )
    )
}
