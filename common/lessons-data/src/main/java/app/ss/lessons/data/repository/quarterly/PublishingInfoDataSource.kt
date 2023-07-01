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

package app.ss.lessons.data.repository.quarterly

import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.PublishingInfo
import app.ss.storage.db.dao.PublishingInfoDao
import app.ss.storage.db.entity.PublishingInfoEntity
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import ss.lessons.api.SSQuarterliesApi
import ss.lessons.model.request.PublishingInfoRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PublishingInfoDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    connectivityHelper: ConnectivityHelper,
    private val publishingInfoDao: PublishingInfoDao,
    private val quarterliesApi: SSQuarterliesApi
) : DataSourceMediator<PublishingInfo, PublishingInfoDataSource.Request>(
    dispatcherProvider = dispatcherProvider,
    connectivityHelper = connectivityHelper
) {

    data class Request(
        val country: String,
        val language: String
    )

    override val cache: LocalDataSource<PublishingInfo, Request> = object : LocalDataSource<PublishingInfo, Request> {
        override suspend fun getItem(request: Request): Resource<PublishingInfo> {
            val info = publishingInfoDao.get(request.country, request.language) ?: return Resource.loading()
            return Resource.success(PublishingInfo(info.message, info.url))
        }

        override suspend fun update(request: Request, data: List<PublishingInfo>) {
            data.forEach {
                publishingInfoDao.insertItem(
                    PublishingInfoEntity(
                        country = request.country,
                        language = request.language,
                        message = it.message,
                        url = it.url
                    )
                )
            }
        }
    }

    override val network: DataSource<PublishingInfo, Request> = object : DataSource<PublishingInfo, Request> {
        override suspend fun getItem(request: Request): Resource<PublishingInfo> {
            val response = quarterliesApi.getPublishingInfo(
                PublishingInfoRequest(
                    request.country,
                    request.language
                )
            )
            return response.body()?.data?.let { Resource.success(it) } ?: Resource.error(Throwable(""))
        }
    }
}
