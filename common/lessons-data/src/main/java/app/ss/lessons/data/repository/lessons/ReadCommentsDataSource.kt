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
import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.SSReadComments
import app.ss.storage.db.dao.ReadCommentsDao
import app.ss.storage.db.entity.ReadCommentsEntity
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReadCommentsDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val lessonsApi: SSLessonsApi,
    private val readCommentsDao: ReadCommentsDao,
) : DataSourceMediator<SSReadComments, ReadCommentsDataSource.Request>(dispatcherProvider = dispatcherProvider) {

    data class Request(val readIndex: String)

    override val cache: LocalDataSource<SSReadComments, Request> = object : LocalDataSource<SSReadComments, Request> {

        override suspend fun getItem(request: Request): Resource<SSReadComments> {
            val data = readCommentsDao.get(request.readIndex)?.let {
                SSReadComments(it.readIndex, it.comments)
            }
            return data?.let { Resource.success(data) } ?: Resource.loading()
        }

        override suspend fun updateItem(data: SSReadComments) {
            if (data.comments.isNotEmpty()) {
                readCommentsDao.insertItem(ReadCommentsEntity(data.readIndex, data.comments))
            }
        }
    }

    override val network: DataSource<SSReadComments, Request> = object : DataSource<SSReadComments, Request> {

        override suspend fun getItem(request: Request): Resource<SSReadComments> = try {
            val response = lessonsApi.getComments(request.readIndex)
            Resource.success(response.body() ?: SSReadComments(request.readIndex, emptyList()))
        } catch (error: Throwable) {
            Timber.e(error)
            Resource.error(error)
        }

        override suspend fun update(request: Request, data: List<SSReadComments>) {
            data.forEach { lessonsApi.uploadComments(it) }
        }
    }
}
