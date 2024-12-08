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

import app.ss.models.feed.FeedResource
import app.ss.models.feed.FeedResourceKind
import app.ss.models.feed.FeedType
import app.ss.models.resource.ResourceCovers

object PlaceHolders {
    val FEED_RESOURCE = FeedResource(
        id = "1",
        name = "name",
        title = "Resource Title",
        startDate = "startDate",
        endDate = "endDate",
        description = "Resource description",
        introduction = "Resource introduction",
        index = "index",
        type = FeedType.DEVO,
        credits = emptyList(),
        features = emptyList(),
        primaryColor = "#d8d8d8",
        primaryColorDark = "#d8d8d8",
        subtitle = "subtitle",
        covers = ResourceCovers(
            portrait = "portrait",
            landscape = "landscape",
            square = "square",
            splash = "splash",
        ),
        kind = FeedResourceKind.BOOK,
    )
}
