/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.storage.db

import androidx.room.TypeConverter
import app.ss.models.Credit
import app.ss.models.Feature
import app.ss.models.LessonPdf
import app.ss.models.QuarterlyGroup
import app.ss.models.SSBibleVerses
import app.ss.models.SSDay
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type

internal object Converters {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val quarterlyGroupAdapter: JsonAdapter<QuarterlyGroup> by lazy {
        moshi.adapter(QuarterlyGroup::class.java)
    }
    private val featuresAdapter: JsonAdapter<List<Feature>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, Feature::class.java)
        moshi.adapter(listDataType)
    }
    private val creditsAdapter: JsonAdapter<List<Credit>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, Credit::class.java)
        moshi.adapter(listDataType)
    }
    private val daysAdapter: JsonAdapter<List<SSDay>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, SSDay::class.java)
        moshi.adapter(listDataType)
    }
    private val pdfsAdapter: JsonAdapter<List<LessonPdf>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, LessonPdf::class.java)
        moshi.adapter(listDataType)
    }
    private val versesAdapter: JsonAdapter<List<SSBibleVerses>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, SSBibleVerses::class.java)
        moshi.adapter(listDataType)
    }

    @TypeConverter
    fun toQuarterlyGroup(value: String?): QuarterlyGroup? = value?.let { jsonString ->
        quarterlyGroupAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromQuarterlyGroup(group: QuarterlyGroup?): String? = quarterlyGroupAdapter.toJson(group)

    @TypeConverter
    fun toFeatures(value: String?): List<Feature>? = value?.let { jsonString ->
        featuresAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromFeatures(features: List<Feature>?): String? = featuresAdapter.toJson(features)

    @TypeConverter
    fun toCredits(value: String?): List<Credit>? = value?.let { jsonString ->
        creditsAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromCredits(credits: List<Credit>?): String? = creditsAdapter.toJson(credits)

    @TypeConverter
    fun toSSDays(value: String?): List<SSDay>? = value?.let { jsonString ->
        daysAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromSSDays(days: List<SSDay>?): String? = daysAdapter.toJson(days)

    @TypeConverter
    fun toLessonPdfs(value: String?): List<LessonPdf>? = value?.let { jsonString ->
        pdfsAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromLessonPdfs(lessons: List<LessonPdf>?): String? = pdfsAdapter.toJson(lessons)

    @TypeConverter
    fun toBibleVerses(value: String?): List<SSBibleVerses>? = value?.let { jsonString ->
        versesAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromBibleVerses(verses: List<SSBibleVerses>?): String? = versesAdapter.toJson(verses)
}
