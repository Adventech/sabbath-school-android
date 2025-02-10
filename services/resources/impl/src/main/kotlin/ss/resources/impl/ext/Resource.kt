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

import io.adventech.blockkit.model.resource.Resource
import ss.libraries.storage.api.entity.ResourceEntity

fun ResourceEntity.toModel() = Resource(
    id = id,
    name = name,
    title = title,
    markdownTitle = markdownTitle,
    startDate = startDate,
    endDate = endDate,
    description = description,
    markdownDescription = markdownDescription,
    introduction = introduction,
    index = index,
    type = type,
    credits = credits,
    features = features,
    primaryColor = primaryColor,
    primaryColorDark = primaryColorDark,
    subtitle = subtitle,
    markdownSubtitle = markdownSubtitle,
    covers = covers,
    kind = kind,
    sectionView = sectionView,
    sections = sections,
    cta = cta,
    preferredCover = preferredCover,
    fonts = fonts,
    style = style,
)

fun Resource.toEntity() = ResourceEntity(
    id = id,
    name = name,
    title = title,
    markdownTitle = markdownTitle,
    startDate = startDate,
    endDate = endDate,
    description = description,
    markdownDescription = markdownDescription,
    introduction = introduction,
    index = index,
    type = type,
    credits = credits,
    features = features,
    primaryColor = primaryColor,
    primaryColorDark = primaryColorDark,
    subtitle = subtitle,
    markdownSubtitle = markdownSubtitle,
    covers = covers,
    kind = kind,
    sectionView = sectionView,
    sections = sections,
    cta = cta,
    preferredCover = preferredCover,
    fonts = fonts,
    style = style,
)
