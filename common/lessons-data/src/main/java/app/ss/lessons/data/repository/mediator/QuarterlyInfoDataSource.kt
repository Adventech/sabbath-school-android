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

package app.ss.lessons.data.repository.mediator

import app.ss.lessons.data.api.SSQuarterliesApi
import app.ss.models.SSQuarterlyInfo
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class QuarterlyInfoDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val quarterliesApi: SSQuarterliesApi,
) : DataSourceMediator<SSQuarterlyInfo, QuarterlyInfoDataSource.Request>(dispatcherProvider = dispatcherProvider) {

    data class Request(val language: String, val id: String)

    override val cache: LocalDataSource<SSQuarterlyInfo, Request> = object : LocalDataSource<SSQuarterlyInfo, Request> {
        override suspend fun getItem(request: Request?): Resource<SSQuarterlyInfo> {
            return Resource.loading()
        }

        override fun updateItem(data: SSQuarterlyInfo) {
        }
    }
    override val network: DataSource<SSQuarterlyInfo, Request> = object : LocalDataSource<SSQuarterlyInfo, Request> {
        override suspend fun getItem(request: Request?): Resource<SSQuarterlyInfo> = try {
            val error = Resource.error<SSQuarterlyInfo>(Throwable(""))
            request?.let {
                val data = quarterliesApi.getQuarterlyInfo(request.language, request.id).body()
                data?.let { Resource.success(data) } ?: error
            } ?: error
        } catch (error: Throwable) {
            Resource.error(error)
        }
    }
}
