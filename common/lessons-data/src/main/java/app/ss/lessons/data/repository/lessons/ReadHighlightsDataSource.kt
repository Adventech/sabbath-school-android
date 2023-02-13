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
import app.ss.lessons.data.model.api.request.UploadHighlightsRequest
import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.SSReadHighlights
import app.ss.storage.db.dao.ReadHighlightsDao
import app.ss.storage.db.entity.ReadHighlightsEntity
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReadHighlightsDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    connectivityHelper: ConnectivityHelper,
    private val lessonsApi: SSLessonsApi,
    private val readHighlightsDao: ReadHighlightsDao
) : DataSourceMediator<SSReadHighlights, ReadHighlightsDataSource.Request>(
    dispatcherProvider = dispatcherProvider,
    connectivityHelper = connectivityHelper
) {

    data class Request(val readIndex: String)

    override val cache: LocalDataSource<SSReadHighlights, Request> = object : LocalDataSource<SSReadHighlights, Request> {
        override suspend fun getItem(request: Request): Resource<SSReadHighlights> {
            val data = readHighlightsDao.get(request.readIndex)?.let {
                SSReadHighlights(it.readIndex, it.highlights)
            } ?: SSReadHighlights(request.readIndex)

            return Resource.success(data)
        }

        override suspend fun updateItem(data: SSReadHighlights) {
            if (data.highlights.isNotEmpty()) {
                readHighlightsDao.insertItem(ReadHighlightsEntity(data.readIndex, data.highlights))
            }
        }

        override suspend fun update(data: List<SSReadHighlights>) {
            data.forEach { updateItem(it) }
        }
    }

    override val network: DataSource<SSReadHighlights, Request> = object : DataSource<SSReadHighlights, Request> {

        override suspend fun getItem(request: Request): Resource<SSReadHighlights> {
            val response = lessonsApi.getHighlights(request.readIndex)
            val highlights = response.body()?.highlights ?: ""
            return Resource.success(SSReadHighlights(request.readIndex, highlights))
        }

        override suspend fun update(request: Request, data: List<SSReadHighlights>) {
            data.forEach { lessonsApi.uploadHighlights(UploadHighlightsRequest(it.readIndex, it.highlights)) }
        }
    }
}
