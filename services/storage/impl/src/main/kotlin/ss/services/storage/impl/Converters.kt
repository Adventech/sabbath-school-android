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

package ss.services.storage.impl

import androidx.room.TypeConverter
import app.ss.models.AppWidgetDay
import app.ss.models.LessonPdf
import app.ss.models.QuarterlyGroup
import app.ss.models.SSBibleVerses
import app.ss.models.SSComment
import app.ss.models.SSDay
import app.ss.models.auth.AccountToken
import app.ss.models.media.SSVideo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.adventech.blockkit.model.resource.Credit
import io.adventech.blockkit.model.resource.Feature
import timber.log.Timber
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
    private val commentsAdapter: JsonAdapter<List<SSComment>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, SSComment::class.java)
        moshi.adapter(listDataType)
    }
    private val stringsAdapter: JsonAdapter<List<String>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, String::class.java)
        moshi.adapter(listDataType)
    }
    private val accountTokenAdapter: JsonAdapter<AccountToken> by lazy {
        moshi.adapter(AccountToken::class.java)
    }
    private val videosAdapter: JsonAdapter<List<SSVideo>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, SSVideo::class.java)
        moshi.adapter(listDataType)
    }
    private val widgetDaysAdapter: JsonAdapter<List<AppWidgetDay>> by lazy {
        val listDataType: Type = Types.newParameterizedType(List::class.java, AppWidgetDay::class.java)
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

    @TypeConverter
    fun toComments(value: String?): List<SSComment>? = try {
        value?.let {
            val jsonString = it
                .replace("\"a\"", "\"elementId\"")
                .replace("\"b\"", "\"comment\"")
            commentsAdapter.fromJson(jsonString)
        }
    } catch (ex: Exception) {
        Timber.e(ex)
        emptyList()
    }

    @TypeConverter
    fun fromComments(comments: List<SSComment>?): String? = commentsAdapter.toJson(comments)

    @TypeConverter
    fun toStrings(value: String?): List<String>? = value?.let { jsonString ->
        stringsAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromStrings(verses: List<String>?): String? = stringsAdapter.toJson(verses)

    @TypeConverter
    fun toAccountToken(value: String?): AccountToken? = value?.let { jsonString ->
        accountTokenAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromAccountToken(token: AccountToken?): String? = accountTokenAdapter.toJson(token)

    @TypeConverter
    fun toVideos(value: String?): List<SSVideo>? = value?.let { jsonString ->
        videosAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromVideos(videos: List<SSVideo>?): String? = videosAdapter.toJson(videos)

    @TypeConverter
    fun toWidgetDays(value: String?): List<AppWidgetDay>? = value?.let { jsonString ->
        widgetDaysAdapter.fromJson(jsonString)
    }

    @TypeConverter
    fun fromWidgetDays(videos: List<AppWidgetDay>?): String? = widgetDaysAdapter.toJson(videos)
}
