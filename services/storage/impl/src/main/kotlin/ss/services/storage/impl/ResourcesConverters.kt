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

package ss.services.storage.impl

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ResourceCTA
import io.adventech.blockkit.model.resource.ResourceCovers
import io.adventech.blockkit.model.resource.ResourceFont
import io.adventech.blockkit.model.resource.ResourceSection
import ss.services.storage.impl.Converters.moshi
import java.lang.reflect.Type

object ResourcesConverters {

    private val coversAdapter: JsonAdapter<ResourceCovers> by lazy {
        moshi.adapter(ResourceCovers::class.java)
    }
    private val ctaAdapter: JsonAdapter<ResourceCTA> by lazy {
        moshi.adapter(ResourceCTA::class.java)
    }
    private val feedGroupsAdapter: JsonAdapter<List<FeedGroup>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, FeedGroup::class.java)
        moshi.adapter(listDataType)
    }
    private val sectionsAdapter: JsonAdapter<List<ResourceSection>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, ResourceSection::class.java)
        moshi.adapter(listDataType)
    }
    private val fontsAdapter: JsonAdapter<List<ResourceFont>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, ResourceFont::class.java)
        moshi.adapter(listDataType)
    }
    private val resourcesAdapter: JsonAdapter<List<Resource>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, Resource::class.java)
        moshi.adapter(listDataType)
    }

    @TypeConverter
    fun toCovers(value: String?): ResourceCovers? = value?.let { jsonString ->
        coversAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromCovers(covers: ResourceCovers?): String? = coversAdapter.toJson(covers)

    @TypeConverter
    fun toSections(value: String?): List<ResourceSection>? = value?.let { jsonString ->
        sectionsAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromSections(sections: List<ResourceSection>?): String? = sectionsAdapter.toJson(sections)

    @TypeConverter
    fun toCTA(value: String?): ResourceCTA? = value?.let { jsonString ->
        ctaAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromCTA(cta: ResourceCTA?): String? = ctaAdapter.toJson(cta)

    @TypeConverter
    fun toFonts(value: String?): List<ResourceFont>? = value?.let { jsonString ->
        fontsAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromFonts(fonts: List<ResourceFont>?): String? = fontsAdapter.toJson(fonts)

    @TypeConverter
    fun toFeedGroups(value: String?): List<FeedGroup>? = value?.let { jsonString ->
        feedGroupsAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromFeedGroups(groups: List<FeedGroup>?): String? = feedGroupsAdapter.toJson(groups)

    @TypeConverter
    fun toResources(value: String?): List<Resource>? = value?.let { jsonString ->
        resourcesAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromResources(resources: List<Resource>?): String? = resourcesAdapter.toJson(resources)

}
