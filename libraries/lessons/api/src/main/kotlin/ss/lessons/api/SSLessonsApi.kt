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

package ss.lessons.api

import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url
import ss.lessons.model.AnnotationsPdf
import ss.lessons.model.ReadComments
import ss.lessons.model.ReadHighlights
import ss.lessons.model.request.UploadPdfAnnotationsRequest

interface SSLessonsApi {

    @GET("api/v2/{lang}/quarterlies/{quarterlyId}/lessons/{lessonId}/index.json")
    suspend fun getLessonInfo(
        @Path("lang") language: String,
        @Path("quarterlyId") quarterlyId: String,
        @Path("lessonId") lessonId: String
    ): Response<SSLessonInfo>

    @GET("api/v2/{lesson}/index.json")
    suspend fun getLessonInfo(
        @Path("lesson") lessonPath: String,
    ): Response<SSLessonInfo>

    @GET
    suspend fun getDayRead(@Url fullPath: String): Response<SSRead>

    @GET("api/v2/annotations/{lessonIndex}/{pdfId}")
    suspend fun getPdfAnnotations(
        @Path("lessonIndex") lessonIndex: String,
        @Path("pdfId") pdfId: String
    ): Response<List<AnnotationsPdf>>

    @POST("api/v2/annotations/{lessonIndex}/{pdfId}")
    suspend fun uploadAnnotations(
        @Path("lessonIndex") lessonIndex: String,
        @Path("pdfId") pdfId: String,
        @Body request: UploadPdfAnnotationsRequest
    ): Response<ResponseBody>

    @GET("api/v2/comments/{readIndex}")
    suspend fun getComments(
        @Path("readIndex") readIndex: String
    ): Response<ReadComments>

    @POST("api/v2/comments")
    suspend fun uploadComments(
        @Body comments: SSReadComments
    ): Response<ResponseBody>

    @GET("api/v2/highlights/{readIndex}")
    suspend fun getHighlights(
        @Path("readIndex") readIndex: String
    ): Response<ReadHighlights>

    @POST("api/v2/highlights")
    suspend fun uploadHighlights(
        @Body highlights: SSReadHighlights
    ): Response<ResponseBody>

    @GET
    suspend fun readerArtifact(
        @Url url: String
    ): Response<ResponseBody>
}
