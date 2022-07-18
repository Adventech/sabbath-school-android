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

import app.ss.models.PdfAnnotations
import app.ss.models.SSDay
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import app.ss.models.TodayData
import app.ss.models.WeekData
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.flow.Flow

interface LessonsRepository {

    suspend fun getLessonInfo(lessonIndex: String, cached: Boolean = false): Resource<SSLessonInfo>

    suspend fun getTodayRead(cached: Boolean = false): Resource<TodayData>

    suspend fun getDayRead(dayIndex: String): Resource<SSRead>

    suspend fun getDayRead(day: SSDay): Resource<SSRead>

    suspend fun getWeekData(cached: Boolean = false): Resource<WeekData>

    suspend fun saveAnnotations(lessonIndex: String, pdfId: String, annotations: List<PdfAnnotations>)

    fun getAnnotations(lessonIndex: String, pdfId: String): Flow<Resource<List<PdfAnnotations>>>

    suspend fun getComments(readIndex: String): Resource<SSReadComments>

    suspend fun saveComments(comments: SSReadComments)

    suspend fun getReadHighlights(readIndex: String): Resource<SSReadHighlights>

    suspend fun saveHighlights(highlights: SSReadHighlights)

    fun checkReaderArtifact()

    suspend fun getPreferredBibleVersion(): String?

    suspend fun savePreferredBibleVersion(version: String)
}
