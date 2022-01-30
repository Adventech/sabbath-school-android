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
import app.ss.lessons.data.model.Language
import app.ss.storage.db.dao.LanguagesDao
import app.ss.storage.db.entity.LanguageEntity
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LanguagesDataSource @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val languagesDao: LanguagesDao,
    private val quarterliesApi: SSQuarterliesApi,
) : DataSourceMediator<Language>(
    schedulerProvider = schedulerProvider
) {
    override val cache: LocalDataSource<Language>
        get() = object : LocalDataSource<Language> {
            override suspend fun get(): Resource<List<Language>> {
                val data = languagesDao.get().map {
                    Language(it.code, it.name)
                }

                return Resource.success(data)
            }

            override fun update(data: List<Language>) {
                languagesDao.insertAll(data.map { LanguageEntity(it.code, it.name) })
            }
        }

    override val network: DataSource<Language>
        get() = object : DataSource<Language> {
            override suspend fun get(): Resource<List<Language>> =
                try {
                    val data = quarterliesApi.getLanguages().body()?.map {
                        Language(it.code, it.name)
                    } ?: emptyList()
                    Resource.success(data)
                } catch (error: Throwable) {
                    Resource.error(error)
                }
        }
}
