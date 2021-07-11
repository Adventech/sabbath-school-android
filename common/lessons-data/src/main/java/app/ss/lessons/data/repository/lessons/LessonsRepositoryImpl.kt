/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package app.ss.lessons.data.repository.lessons

import app.ss.lessons.data.extensions.ValueEvent
import app.ss.lessons.data.extensions.singleEvent
import app.ss.lessons.data.model.QuarterlyLessonInfo
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.model.SSQuarterlyInfo
import app.ss.lessons.data.model.SSRead
import app.ss.lessons.data.model.TodayData
import app.ss.lessons.data.model.WeekData
import app.ss.lessons.data.model.WeekDay
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.DateHelper.formatDate
import com.cryart.sabbathschool.core.misc.DateHelper.parseDate
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import com.google.firebase.database.FirebaseDatabase
import org.joda.time.DateTime

internal class LessonsRepositoryImpl constructor(
    firebaseDatabase: FirebaseDatabase,
    private val ssPrefs: SSPrefs
) : LessonsRepository {

    private val firebaseRef = firebaseDatabase.reference.apply { keepSynced(true) }

    override suspend fun getLessonInfo(lessonIndex: String): Resource<SSLessonInfo> {
        val event = firebaseRef
            .child(SSConstants.SS_FIREBASE_LESSON_INFO_DATABASE)
            .child(lessonIndex)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> Resource.error(event.error)
            is ValueEvent.DataChange -> Resource.success(SSLessonInfo(event.snapshot))
        }
    }

    override suspend fun getTodayRead(): Resource<TodayData> {
        val dataResponse = getQuarterlyAndLessonInfo()
        val lessonInfo: SSLessonInfo
        if (dataResponse.isSuccessFul && dataResponse.data != null) {
            lessonInfo = dataResponse.data!!.lessonInfo
        } else {
            return Resource.error(dataResponse.error ?: Throwable("Invalid QuarterlyInfo"))
        }

        val today = DateTime.now().withTimeAtStartOfDay()
        val todayModel = lessonInfo.days.find { day ->
            today.isEqual(parseDate(day.date))
        }?.let { day ->
            TodayData(
                day.index,
                lessonInfo.lesson.index,
                day.title,
                formatDate(day.date),
                lessonInfo.lesson.cover
            )
        } ?: return Resource.error(Throwable("Error Finding Today Read"))

        return Resource.success(todayModel)
    }

    private suspend fun getQuarterlyAndLessonInfo(): Resource<QuarterlyLessonInfo> {
        val quarterlyResponse = getQuarterlyInfo()
        val quarterlyInfo: SSQuarterlyInfo
        if (quarterlyResponse.isSuccessFul && quarterlyResponse.data != null) {
            quarterlyInfo = quarterlyResponse.data!!
        } else {
            return Resource.error(quarterlyResponse.error ?: Throwable("Invalid QuarterlyInfo"))
        }
        val lessonInfo = getWeekLessonInfo(quarterlyInfo) ?: return Resource.error(Throwable("Invalid LessonInfo"))

        return Resource.success(QuarterlyLessonInfo(quarterlyInfo, lessonInfo))
    }

    private suspend fun getQuarterlyInfo(): Resource<SSQuarterlyInfo> {
        var code = ssPrefs.getLanguageCode()
        if (code == "iw") {
            code = "he"
        }
        if (code == "fil") {
            code = "tl"
        }

        val quarterlyEvent = firebaseRef
            .child(SSConstants.SS_FIREBASE_QUARTERLIES_DATABASE)
            .child(code)
            .singleEvent()

        val quarterlyIndex = when (quarterlyEvent) {
            is ValueEvent.Cancelled -> null
            is ValueEvent.DataChange -> {
                val quarterlies = quarterlyEvent.snapshot.children.mapNotNull {
                    it.getValue(SSQuarterly::class.java)
                }
                val quarterly = quarterlies.firstOrNull()
                quarterly?.index
            }
        }

        val index = quarterlyIndex ?: return Resource.error(Throwable("Invalid Quarterly Index"))

        val event = firebaseRef
            .child(SSConstants.SS_FIREBASE_QUARTERLY_INFO_DATABASE)
            .child(index)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> Resource.error(event.error)
            is ValueEvent.DataChange -> Resource.success(SSQuarterlyInfo(event.snapshot))
        }
    }

    private suspend fun getWeekLessonInfo(quarterlyInfo: SSQuarterlyInfo): SSLessonInfo? {
        val lesson = quarterlyInfo.lessons.find { lesson ->
            val startDate = parseDate(lesson.start_date)
            val endDate = parseDate(lesson.end_date)

            val today = DateTime.now().withTimeAtStartOfDay()
            startDate?.isBeforeNow == true && (endDate?.isAfterNow == true || today.isEqual(endDate))
        }

        return lesson?.let { getLessonInfo(it.index).data }
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> {
        val event = firebaseRef
            .child(SSConstants.SS_FIREBASE_READS_DATABASE)
            .child(dayIndex)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> Resource.error(event.error)
            is ValueEvent.DataChange -> Resource.success(SSRead(event.snapshot))
        }
    }

    override suspend fun getWeekData(): Resource<WeekData> {
        val dataResponse = getQuarterlyAndLessonInfo()
        val quarterlyInfo: SSQuarterlyInfo
        val lessonInfo: SSLessonInfo
        if (dataResponse.isSuccessFul && dataResponse.data != null) {
            quarterlyInfo = dataResponse.data!!.quarterlyInfo
            lessonInfo = dataResponse.data!!.lessonInfo
        } else {
            return Resource.error(dataResponse.error ?: Throwable("Invalid QuarterlyInfo"))
        }

        val today = DateTime.now().withTimeAtStartOfDay()

        val days = lessonInfo.days.map { ssDay ->
            WeekDay(
                ssDay.index,
                ssDay.title,
                formatDate(ssDay.date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY_SHORT),
                today.isEqual(parseDate(ssDay.date))
            )
        }.take(7)

        return Resource.success(
            WeekData(
                quarterlyInfo.quarterly.index,
                quarterlyInfo.quarterly.title,
                lessonInfo.lesson.index,
                lessonInfo.lesson.title,
                quarterlyInfo.quarterly.cover,
                days
            )
        )
    }
}
