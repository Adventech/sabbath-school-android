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

package app.ss.lessons.data.repository.quarterly

import androidx.annotation.VisibleForTesting
import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.Language
import app.ss.storage.db.dao.LanguagesDao
import app.ss.storage.db.entity.LanguageEntity
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import ss.lessons.api.SSQuarterliesApi
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LanguagesDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    connectivityHelper: ConnectivityHelper,
    private val languagesDao: LanguagesDao,
    private val quarterliesApi: SSQuarterliesApi
) : DataSourceMediator<Language, LanguagesDataSource.Request>(
    dispatcherProvider = dispatcherProvider,
    connectivityHelper = connectivityHelper
) {
    data class Request(val query: String? = null)

    override val cache: LocalDataSource<Language, Request> = object : LocalDataSource<Language, Request> {
        override suspend fun get(request: Request): Resource<List<Language>> {
            val query = request.query
            val data =
                if (query.isNullOrEmpty()) {
                    languagesDao.get()
                } else {
                    languagesDao.search("%$query%")
                }

            return Resource.success(
                data.map { language ->
                    Language(
                        code = language.code,
                        name = language.name,
                        nativeName = language.nativeName.takeUnless { it.isEmpty() } ?: getNativeLanguageName(language.code, language.name)
                    )
                }
            )
        }

        override suspend fun update(data: List<Language>) {
            languagesDao.insertAll(data.map { LanguageEntity(it.code, it.name, it.nativeName) })
        }
    }

    override val network: DataSource<Language, Request> = object : DataSource<Language, Request> {
        override suspend fun get(request: Request): Resource<List<Language>> {
            if (request.query != null) return Resource.loading()

            val data = quarterliesApi.getLanguages().body()?.map {
                Language(it.code, it.name, getNativeLanguageName(it.code, it.name))
            } ?: emptyList()
            return Resource.success(data)
        }
    }

    @VisibleForTesting
    internal fun getNativeLanguageName(languageCode: String, languageName: String): String {
        val loc = Locale(languageCode)
        val name = loc.getDisplayLanguage(loc).takeUnless { it == languageCode } ?: languageName
        return name.replaceFirstChar { it.uppercase() }
    }
}
