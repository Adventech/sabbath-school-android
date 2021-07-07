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

import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.model.SSQuarterlyInfo
import app.ss.lessons.data.model.SSRead
import app.ss.lessons.data.model.TodayModel
import com.cryart.sabbathschool.core.extensions.logger.timber
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.DateHelper.formatDate
import com.cryart.sabbathschool.core.misc.DateHelper.parseDate
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.joda.time.DateTime
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class LessonsRepositoryImpl constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val ssPrefs: SSPrefs
) : LessonsRepository {

    private val logger by timber()

    private val firebaseRef = firebaseDatabase.reference.apply { keepSynced(true) }

    override suspend fun getLessonInfo(lessonIndex: String): Resource<SSLessonInfo> = suspendCoroutine { continuation ->
        firebaseRef
            .child(SSConstants.SS_FIREBASE_LESSON_INFO_DATABASE)
            .child(lessonIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = SSLessonInfo(snapshot)
                    continuation.resume(Resource.success(data))
                }

                override fun onCancelled(error: DatabaseError) {
                    logger.e(error.toException())
                    continuation.resumeWithException(error.toException())
                }
            })
    }

    override suspend fun getTodayRead(): Resource<TodayModel?> {
        val index = ssPrefs.getLastQuarterlyIndex() ?: findQuarterlyIndex() ?: return Resource.error(Throwable("Invalid Quarterly Index"))

        return suspendCoroutine { continuation ->
            firebaseDatabase.getReference(
                SSConstants.SS_FIREBASE_QUARTERLY_INFO_DATABASE
            )
                .child(index)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val quarterlyInfo = SSQuarterlyInfo(snapshot)

                        quarterlyInfo.lessons.forEach { lesson ->
                            val startDate = parseDate(lesson.start_date)
                            val endDate = parseDate(lesson.end_date)

                            if (startDate?.isBeforeNow == true && endDate?.isAfterNow == true) {
                                findTodayRead(lesson.index) { model ->
                                    continuation.resume(Resource.success(model))
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        logger.e(error.toException())
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    private fun findTodayRead(lessonIndex: String, callback: (TodayModel?) -> Unit) {
        firebaseDatabase
            .getReference(SSConstants.SS_FIREBASE_LESSON_INFO_DATABASE)
            .child(lessonIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lessonInfo = SSLessonInfo(snapshot)
                    val today = DateTime.now().withTimeAtStartOfDay()

                    lessonInfo.days.forEach { day ->
                        val date = parseDate(day.date)
                        if (date?.isEqual(today) == true) {
                            val model = TodayModel(
                                day.index,
                                lessonInfo.lesson.index,
                                day.title,
                                formatDate(day.date),
                                lessonInfo.lesson.cover
                            )

                            callback(model)
                            return@forEach
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    logger.e(error.toException())
                    callback(null)
                }
            })
    }

    private suspend fun findQuarterlyIndex(): String? = suspendCoroutine { continuation ->
        var code = ssPrefs.getLanguageCode()
        if (code == "iw") {
            code = "he"
        }
        if (code == "fil") {
            code = "tl"
        }

        firebaseDatabase.getReference(
            SSConstants.SS_FIREBASE_QUARTERLIES_DATABASE
        )
            .child(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val quarterlies = snapshot.children.mapNotNull {
                        it.getValue(SSQuarterly::class.java)
                    }
                    val quarterly = quarterlies.firstOrNull()

                    continuation.resume(quarterly?.index)
                }

                override fun onCancelled(error: DatabaseError) {
                    logger.e(error.toException())
                    continuation.resumeWithException(error.toException())
                }
            })
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> = suspendCoroutine { continuation ->
        firebaseRef
            .child(SSConstants.SS_FIREBASE_READS_DATABASE)
            .child(dayIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val ssRead = SSRead(dataSnapshot)
                    continuation.resume(Resource.success(ssRead))
                }

                override fun onCancelled(error: DatabaseError) {
                    logger.e(error.toException())
                    continuation.resumeWithException(error.toException())
                }
            })
    }
}
