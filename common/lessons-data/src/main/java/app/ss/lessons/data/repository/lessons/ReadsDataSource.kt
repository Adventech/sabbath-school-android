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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.lessons.data.repository.lessons

import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.SSRead
import app.ss.storage.db.dao.ReadsDao
import app.ss.storage.db.entity.ReadEntity
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import ss.lessons.api.SSLessonsApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReadsDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    connectivityHelper: ConnectivityHelper,
    private val lessonsApi: SSLessonsApi,
    private val readsDao: ReadsDao
) : DataSourceMediator<SSRead, ReadsDataSource.Request>(
    dispatcherProvider = dispatcherProvider,
    connectivityHelper = connectivityHelper
) {

    data class Request(
        val dayIndex: String,
        val fullPath: String
    )

    override val cache: LocalDataSource<SSRead, Request> = object : LocalDataSource<SSRead, Request> {

        override suspend fun getItem(request: Request): Resource<SSRead> {
            val read = readsDao.get(request.dayIndex) ?: return Resource.loading()
            return Resource.success(read.toModel())
        }

        override suspend fun updateItem(data: SSRead) {
            readsDao.insertItem(data.toEntity())
        }
    }

    override val network: DataSource<SSRead, Request> = object : DataSource<SSRead, Request> {
        override suspend fun getItem(request: Request): Resource<SSRead> {
            val error = Resource.error<SSRead>(Throwable(""))
            val response = lessonsApi.getDayRead("${request.fullPath}/index.json")
            return response.body()?.let { Resource.success(it) } ?: error
        }
    }

    private fun SSRead.toEntity(): ReadEntity = ReadEntity(
        index = index,
        id = id,
        date = date,
        title = title,
        content = content,
        bible = bible
    )

    private fun ReadEntity.toModel(): SSRead = SSRead(
        index = index,
        id = id,
        date = date,
        title = title,
        content = content,
        bible = bible
    )
}
