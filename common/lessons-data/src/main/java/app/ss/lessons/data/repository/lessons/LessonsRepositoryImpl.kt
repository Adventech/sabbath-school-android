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
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.model.SSQuarterlyInfo
import app.ss.lessons.data.model.SSRead
import app.ss.lessons.data.model.TodayData
import com.cryart.sabbathschool.core.extensions.logger.timber
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

    private val logger by timber()

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

    override suspend fun getTodayRead(): Resource<TodayData?> {
        val index = findQuarterlyIndex() ?: return Resource.success(null)

        val event = firebaseRef
            .child(SSConstants.SS_FIREBASE_QUARTERLY_INFO_DATABASE)
            .child(index)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> Resource.error(event.error)
            is ValueEvent.DataChange -> {
                val quarterlyInfo = SSQuarterlyInfo(event.snapshot)

                val todayModel = quarterlyInfo.lessons.find { lesson ->
                    val startDate = parseDate(lesson.start_date)
                    val endDate = parseDate(lesson.end_date)

                    val today = DateTime.now().withTimeAtStartOfDay()
                    startDate?.isBeforeNow == true && (endDate?.isAfterNow == true || today.isEqual(endDate))
                }?.let { findTodayRead(it.index) }

                Resource.success(todayModel)
            }
        }
    }

    private suspend fun findTodayRead(lessonIndex: String): TodayData? {
        val event = firebaseRef
            .child(SSConstants.SS_FIREBASE_LESSON_INFO_DATABASE)
            .child(lessonIndex)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> null
            is ValueEvent.DataChange -> {
                val lessonInfo = SSLessonInfo(event.snapshot)
                val today = DateTime.now().withTimeAtStartOfDay()

                val todayModel = lessonInfo.days.find { day ->
                    val date = parseDate(day.date)
                    date?.isEqual(today) == true
                }?.let { day ->
                    TodayData(
                        day.index,
                        lessonInfo.lesson.index,
                        day.title,
                        formatDate(day.date),
                        lessonInfo.lesson.cover
                    )
                }
                todayModel
            }
        }
    }

    private suspend fun findQuarterlyIndex(): String? {
        var code = ssPrefs.getLanguageCode()
        if (code == "iw") {
            code = "he"
        }
        if (code == "fil") {
            code = "tl"
        }

        val event = firebaseRef
            .child(SSConstants.SS_FIREBASE_QUARTERLIES_DATABASE)
            .child(code)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> null
            is ValueEvent.DataChange -> {
                val quarterlies = event.snapshot.children.mapNotNull {
                    it.getValue(SSQuarterly::class.java)
                }
                val quarterly = quarterlies.firstOrNull()
                quarterly?.index
            }
        }
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
}
