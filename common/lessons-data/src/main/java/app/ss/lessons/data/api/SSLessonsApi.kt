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

package app.ss.lessons.data.api

import app.ss.models.PdfAnnotations
import app.ss.lessons.data.model.api.request.UploadPdfAnnotationsRequest
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SSLessonsApi {

    @GET("api/v2/{lang}/quarterlies/{quarterlyId}/lessons/{lessonId}/index.json")
    suspend fun getLessonInfo(
        @Path("lang") language: String,
        @Path("quarterlyId") quarterlyId: String,
        @Path("lessonId") lessonId: String,
    ): Response<SSLessonInfo>

    @GET("api/v2/{lang}/quarterlies/{quarterlyId}/lessons/{lessonId}/days/{dayId}/read/index.json")
    suspend fun getDayRead(
        @Path("lang") language: String,
        @Path("quarterlyId") quarterlyId: String,
        @Path("lessonId") lessonId: String,
        @Path("dayId") dayId: String,
    ): Response<SSRead>

    @GET("api/v2/annotations/{lessonIndex}/{pdfId}")
    suspend fun getPdfAnnotations(
        @Path("lessonIndex") lessonIndex: String,
        @Path("pdfId") pdfId: String,
    ): Response<List<PdfAnnotations>>

    @POST("api/v2/annotations/{lessonIndex}/{pdfId}")
    suspend fun uploadAnnotations(
        @Path("lessonIndex") lessonIndex: String,
        @Path("pdfId") pdfId: String,
        @Body request: UploadPdfAnnotationsRequest
    ): Response<ResponseBody>
}
