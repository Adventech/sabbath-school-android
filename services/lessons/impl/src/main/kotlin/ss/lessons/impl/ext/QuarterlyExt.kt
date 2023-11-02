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

package ss.lessons.impl.ext

import app.ss.models.OfflineState
import app.ss.models.SSQuarterly
import app.ss.storage.db.entity.QuarterlyEntity

internal fun SSQuarterly.toEntity(
    offlineState: OfflineState
): QuarterlyEntity = QuarterlyEntity(
    index = index,
    id = id,
    title = title,
    description = description,
    introduction = introduction,
    human_date = human_date,
    start_date = start_date,
    end_date = end_date,
    cover = cover,
    splash = splash,
    path = path,
    full_path = full_path,
    lang = lang,
    color_primary = color_primary,
    color_primary_dark = color_primary_dark,
    quarterly_name = quarterly_name,
    quarterly_group = quarterly_group,
    features = features,
    credits = credits,
    offlineState = offlineState,
)

internal fun QuarterlyEntity.toModel(): SSQuarterly = SSQuarterly(
    id = id,
    title = title,
    description = description,
    introduction = introduction,
    human_date = human_date,
    start_date = start_date,
    end_date = end_date,
    cover = cover,
    splash = splash,
    index = index,
    path = path,
    full_path = full_path,
    lang = lang,
    color_primary = color_primary,
    color_primary_dark = color_primary_dark,
    quarterly_name = quarterly_name,
    quarterly_group = quarterly_group,
    features = features,
    credits = credits,
    offlineState = offlineState
)
