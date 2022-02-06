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
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.storage.db.dao.QuarterliesDao
import app.ss.storage.db.entity.QuarterlyEntity
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class QuarterliesDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val quarterliesDao: QuarterliesDao,
    private val quarterliesApi: SSQuarterliesApi,
) : DataSourceMediator<SSQuarterly, QuarterliesDataSource.Request>(dispatcherProvider) {

    data class Request(val language: String, val group: QuarterlyGroup? = null)

    override val cache: LocalDataSource<SSQuarterly, Request> = object : LocalDataSource<SSQuarterly, Request> {
        override suspend fun get(request: Request): Resource<List<SSQuarterly>> {
            val code = request.language
            val group = request.group

            val quarterlies = group?.let { quarterliesDao.get(code, it) } ?: quarterliesDao.get(code)
            val data = quarterlies.map { it.toModel() }
            if (data.isEmpty()) {
                return Resource.loading()
            }
            return Resource.success(data)
        }

        override fun update(data: List<SSQuarterly>) {
            quarterliesDao.insertAll(
                data.map { it.toEntity() }
            )
        }
    }
    override val network: DataSource<SSQuarterly, Request> = object : DataSource<SSQuarterly, Request> {
        override suspend fun get(request: Request): Resource<List<SSQuarterly>> = try {
            val group = request.group
            val data = quarterliesApi.getQuarterlies(request.language).body() ?: emptyList()
            val filtered = group?.let { data.filter { it.quarterly_group == group } } ?: data
            Resource.success(filtered)
        } catch (error: Throwable) {
            Resource.error(error)
        }
    }
}

internal fun QuarterlyEntity.toModel(): SSQuarterly = SSQuarterly(
    id = id,
    title = title,
    description = description,
    introduction = introduction,
    human_date = human_date,
    start_date = start_date,
    end_date = end_date,
    cover = cover,
    splash = splash,
    index = index,
    path = path,
    full_path = full_path,
    lang = lang,
    color_primary = color_primary,
    color_primary_dark = color_primary_dark,
    quarterly_name = quarterly_name,
    quarterly_group = quarterly_group,
    features = features,
    credits = credits
)

internal fun SSQuarterly.toEntity(): QuarterlyEntity = QuarterlyEntity(
    index = index,
    id = id,
    title = title,
    description = description,
    introduction = introduction,
    human_date = human_date,
    start_date = start_date,
    end_date = end_date,
    cover = cover,
    splash = splash,
    path = path,
    full_path = full_path,
    lang = lang,
    color_primary = color_primary,
    color_primary_dark = color_primary_dark,
    quarterly_name = quarterly_name,
    quarterly_group = quarterly_group,
    features = features,
    credits = credits
)
