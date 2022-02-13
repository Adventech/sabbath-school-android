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
import app.ss.lessons.data.repository.mediator.DataSource
import app.ss.lessons.data.repository.mediator.DataSourceMediator
import app.ss.lessons.data.repository.mediator.LocalDataSource
import app.ss.models.SSRead
import app.ss.storage.db.dao.ReadsDao
import app.ss.storage.db.entity.ReadEntity
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReadsDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val lessonsApi: SSLessonsApi,
    private val readsDao: ReadsDao,
) : DataSourceMediator<SSRead, ReadsDataSource.Request>(dispatcherProvider = dispatcherProvider) {

    data class Request(val dayIndex: String)

    override val cache: LocalDataSource<SSRead, Request> = object : LocalDataSource<SSRead, Request> {

        override suspend fun getItem(request: Request): Resource<SSRead> {
            val read = readsDao.get(request.dayIndex) ?: return Resource.loading()
            return Resource.success(read.toModel())
        }

        override fun updateItem(data: SSRead) {
            readsDao.updateItem(data.toEntity())
        }
    }

    override val network: DataSource<SSRead, Request> = object : DataSource<SSRead, Request> {
        override suspend fun getItem(request: Request): Resource<SSRead> = try {
            val error = Resource.error<SSRead>(Throwable(""))
            val index = request.dayIndex

            val language = index.substringBefore('-')
            val dayId = index.substringAfterLast('-')
            val lessonIndex = index.substringAfter('-')
                .substringBeforeLast('-')
            val quarterlyId = lessonIndex.substringBeforeLast('-')
            val lessonId = lessonIndex.substringAfterLast('-')

            val response = lessonsApi.getDayRead(language, quarterlyId, lessonId, dayId)

            response.body()?.let { Resource.success(it) } ?: error
        } catch (error: Throwable) {
            Timber.e(error)
            Resource.error(error)
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