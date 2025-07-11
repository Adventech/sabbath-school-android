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

package io.adventech.blockkit.model.resource

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import io.adventech.blockkit.model.Style
import io.adventech.blockkit.model.feed.FeedResourceKind
import io.adventech.blockkit.model.feed.FeedType

@Keep
@JsonClass(generateAdapter = true)
data class Resource(
    val id: String,
    val name: String,
    val title: String,
    val markdownTitle: String?,
    val startDate: String?,
    val endDate: String?,
    val description: String?,
    val markdownDescription: String?,
    val introduction: String?,
    val index: String,
    val type: FeedType,
    val credits: List<Credit>,
    val features: List<Feature>,
    val primaryColor: String,
    val primaryColorDark: String,
    val subtitle: String?,
    val markdownSubtitle: String?,
    val covers: ResourceCovers,
    val kind: FeedResourceKind,
    val sectionView: ResourceSectionViewType?,
    val sections: List<ResourceSection>?,
    val cta: ResourceCTA?,
    val preferredCover: ResourcePreferredCover?,
    val fonts: List<ResourceFont>? = null,
    val style: Style? = null,
    val progressTracking: ProgressTracking? = null,
    val downloadable: Boolean? = null,
    val share: ShareOptions? = null,
)
