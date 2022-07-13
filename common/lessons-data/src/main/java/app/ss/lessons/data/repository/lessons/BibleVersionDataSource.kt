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

import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.PreferredBibleVersion
import app.ss.storage.db.dao.BibleVersionDao
import app.ss.storage.db.entity.BibleVersionEntity
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BibleVersionDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    connectivityHelper: ConnectivityHelper,
    private val bibleVersionDao: BibleVersionDao
) : DataSourceMediator<PreferredBibleVersion?, BibleVersionDataSource.Request>(
    dispatcherProvider = dispatcherProvider,
    connectivityHelper = connectivityHelper
) {

    data class Request(val language: String)

    override val cache: LocalDataSource<PreferredBibleVersion?, Request> = object : LocalDataSource<PreferredBibleVersion?, Request> {
        override suspend fun getItem(request: Request): Resource<PreferredBibleVersion?> {
            val version = bibleVersionDao.get(request.language)

            return Resource.success(
                version?.let {
                    PreferredBibleVersion(it.version, it.language)
                }
            )
        }

        override suspend fun updateItem(data: PreferredBibleVersion?) {
            data?.let {
                bibleVersionDao.insertItem(BibleVersionEntity(data.language, data.version))
            }
        }
    }

    override val network: DataSource<PreferredBibleVersion?, Request> = object : LocalDataSource<PreferredBibleVersion?, Request> {}
}
