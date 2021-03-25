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
import app.ss.lessons.data.response.Resource
import com.cryart.sabbathschool.core.misc.SSConstants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class LessonsRepositoryImpl constructor(
    firebaseDatabase: FirebaseDatabase
) : LessonsRepository {

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
                    Timber.e(error.toException())
                    continuation.resumeWithException(error.toException())
                }
            })
    }
}
