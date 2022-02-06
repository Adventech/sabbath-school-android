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
import app.ss.models.SSRead
import app.ss.lessons.data.repository.mediator.DataSource
import app.ss.lessons.data.repository.mediator.DataSourceMediator
import app.ss.lessons.data.repository.mediator.LocalDataSource
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReadDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val lessonsApi: SSLessonsApi,
) : DataSourceMediator<SSRead, ReadDataSource.Request>(dispatcherProvider = dispatcherProvider) {

    data class Request(val dayIndex: String)

    override val cache: LocalDataSource<SSRead, Request> = object : LocalDataSource<SSRead, Request> {
    }

    override val network: DataSource<SSRead, Request> = object : DataSource<SSRead, Request> {
        override suspend fun getItem(request: Request?): Resource<SSRead> = try {
            val error = Resource.error<SSRead>(Throwable(""))
            request?.dayIndex?.let { index ->
                val language = index.substringBefore('-')
                val dayId = index.substringAfterLast('-')
                val lessonIndex = index.substringAfter('-')
                    .substringBeforeLast('-')
                val quarterlyId = lessonIndex.substringBeforeLast('-')
                val lessonId = lessonIndex.substringAfterLast('-')

                val data = lessonsApi.getDayRead(language, quarterlyId, lessonId, dayId).body()
                data?.let { Resource.success(it) } ?: error
            } ?: error
        } catch (error: Throwable) {
            Timber.e(error)
            Resource.error(error)
        }
    }
}
