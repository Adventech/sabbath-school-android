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
) : DataSourceMediator<SSQuarterly>(dispatcherProvider) {

    override val cache: LocalDataSource<SSQuarterly> = object : LocalDataSource<SSQuarterly> {
        override suspend fun get(params: Map<String, Any>): Resource<List<SSQuarterly>> {
            val code = params[LANGUAGE] as? String ?: "en"
            val data = quarterliesDao.get(code).map { it.toModel() }
            return Resource.success(data)
        }

        override fun update(data: List<SSQuarterly>) {
            quarterliesDao.insertAll(
                data.map { it.toEntity() }
            )
        }
    }
    override val network: DataSource<SSQuarterly> = object : DataSource<SSQuarterly> {
        override suspend fun get(params: Map<String, Any>): Resource<List<SSQuarterly>> = try {
            val code = params[LANGUAGE] as? String ?: "en"
            val data = quarterliesApi.getQuarterlies(code).body() ?: emptyList()
            Resource.success(data)
        } catch (error: Throwable) {
            Resource.error(error)
        }
    }

    companion object {
        const val LANGUAGE = "language"
    }
}

private fun QuarterlyEntity.toModel(): SSQuarterly = SSQuarterly(
    id, title, description, introduction, human_date, start_date, end_date, cover, splash, index, path,
    full_path, lang, color_primary, color_primary_dark, quarterly_name, quarterly_group, features, credits
)

private fun SSQuarterly.toEntity(): QuarterlyEntity = QuarterlyEntity(
    id, title, description, introduction, human_date, start_date, end_date, cover, splash, index, path,
    full_path, lang, color_primary, color_primary_dark, quarterly_name, quarterly_group, features, credits
)
