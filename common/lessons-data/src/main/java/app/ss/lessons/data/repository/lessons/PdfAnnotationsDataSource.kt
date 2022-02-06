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

package app.ss.lessons.data.repository.lessons

import app.ss.lessons.data.api.SSLessonsApi
import app.ss.lessons.data.model.api.request.UploadPdfAnnotationsRequest
import app.ss.lessons.data.repository.mediator.DataSource
import app.ss.lessons.data.repository.mediator.DataSourceMediator
import app.ss.lessons.data.repository.mediator.LocalDataSource
import app.ss.models.PdfAnnotations
import app.ss.storage.db.dao.PdfAnnotationsDao
import app.ss.storage.db.entity.PdfAnnotationsEntity
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PdfAnnotationsDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val lessonsApi: SSLessonsApi,
    private val pdfAnnotationsDao: PdfAnnotationsDao,
) : DataSourceMediator<PdfAnnotations, PdfAnnotationsDataSource.Request>(dispatcherProvider = dispatcherProvider) {

    data class Request(
        val lessonIndex: String,
        val pdfId: String
    )

    override val cache: LocalDataSource<PdfAnnotations, Request> = object : LocalDataSource<PdfAnnotations, Request> {

        override suspend fun update(request: Request, data: List<PdfAnnotations>) {
            val pdfIndex = "${request.lessonIndex}-${request.pdfId}"
            val entities = data.map {
                PdfAnnotationsEntity(
                    index = "$pdfIndex-${it.pageIndex}",
                    pdfIndex = pdfIndex,
                    pageIndex = it.pageIndex,
                    annotations = it.annotations
                )
            }
            pdfAnnotationsDao.insertAll(entities)
        }

        override suspend fun get(request: Request): Resource<List<PdfAnnotations>> {
            val pdfIndex = "${request.lessonIndex}-${request.pdfId}"
            val data = pdfAnnotationsDao.get(pdfIndex).map {
                PdfAnnotations(pageIndex = it.pageIndex, annotations = it.annotations)
            }
            return Resource.success(data)
        }
    }

    override val network: DataSource<PdfAnnotations, Request> = object : DataSource<PdfAnnotations, Request> {
        override suspend fun get(request: Request): Resource<List<PdfAnnotations>> = try {
            val data = lessonsApi.getPdfAnnotations(request.lessonIndex, request.pdfId).body() ?: emptyList()

            Resource.success(data)
        } catch (error: Throwable) {
            Timber.e(error)
            Resource.error(error)
        }

        override suspend fun update(request: Request, data: List<PdfAnnotations>) = try {
            val response = lessonsApi.uploadAnnotations(
                request.lessonIndex,
                request.pdfId,
                UploadPdfAnnotationsRequest(data)
            )
            Timber.d("SUCCESS: ${response.isSuccessful}")
        } catch (error: Throwable) {
            Timber.e(error)
        }
    }
}
