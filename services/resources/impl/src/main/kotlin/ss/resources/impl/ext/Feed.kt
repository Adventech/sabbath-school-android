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

package ss.resources.impl.ext

import app.ss.models.resource.FeedResponse
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.feed.FeedType
import ss.libraries.storage.api.entity.FeedEntity
import ss.libraries.storage.api.entity.FeedGroupEntity
import ss.resources.model.FeedModel

fun FeedResponse.toEntity(language: String, type: FeedType) = FeedEntity(
    id = "${language}-${type.name.lowercase()}",
    language = language,
    type = type,
    title = title,
    groups = groups,
)

fun FeedEntity.toModel() = FeedModel(title = title, groups)

fun FeedGroup.toEntity() = FeedGroupEntity(
    id = id,
    type = type,
    scope = scope,
    direction = direction,
    title = title,
    view = view,
    resources = resources,
    seeAll = seeAll,
)

fun FeedGroupEntity.toModel() = FeedGroup(
    id = id,
    type = type,
    scope = scope,
    direction = direction,
    title = title,
    view = view,
    resources = resources,
    seeAll = seeAll,
)
