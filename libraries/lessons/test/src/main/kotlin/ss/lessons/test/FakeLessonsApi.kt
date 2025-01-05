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

package ss.lessons.test

import androidx.annotation.VisibleForTesting
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import okhttp3.ResponseBody
import retrofit2.Response
import ss.lessons.api.SSLessonsApi
import ss.lessons.model.AnnotationsPdf
import ss.lessons.model.ReadComments
import ss.lessons.model.ReadHighlights
import ss.lessons.model.request.UploadPdfAnnotationsRequest

/** Fake implementation of [SSLessonsApi]. **/
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeLessonsApi : SSLessonsApi {

    // region Mock behavior

    private var lessonInfoMap: MutableMap<String, Response<SSLessonInfo>> = mutableMapOf()
    private var dayReadMap: MutableMap<String, Response<SSRead>> = mutableMapOf()

    fun addLessonInfoResponse(language: String, quarterlyId: String, lessonId: String, response: Response<SSLessonInfo>) {
        val key = "$language/$quarterlyId/$lessonId"
        lessonInfoMap[key] = response
    }

    fun addDayReadResponse(fullPath: String, response: Response<SSRead>) {
        dayReadMap[fullPath] = response
    }

    // end region

    override suspend fun getLessonInfo(language: String, quarterlyId: String, lessonId: String): Response<SSLessonInfo> {
        val key = "$language/$quarterlyId/$lessonId"
        return lessonInfoMap[key]!!
    }

    override suspend fun getLessonInfo(lessonPath: String): Response<SSLessonInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun getDayRead(fullPath: String): Response<SSRead> = dayReadMap[fullPath]!!

    override suspend fun getPdfAnnotations(lessonIndex: String, pdfId: String): Response<List<AnnotationsPdf>> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadAnnotations(lessonIndex: String, pdfId: String, request: UploadPdfAnnotationsRequest): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getComments(readIndex: String): Response<ReadComments> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadComments(comments: SSReadComments): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getHighlights(readIndex: String): Response<ReadHighlights> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadHighlights(highlights: SSReadHighlights): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun readerArtifact(url: String): Response<ResponseBody> {
        TODO("Not yet implemented")
    }
}
