/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package ss.lessons.impl.repository

import app.ss.models.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.repository.LanguagesRepository
import ss.lessons.impl.helper.SyncHelper
import ss.libraries.storage.api.dao.LanguagesDao
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

internal class LanguagesRepositoryImpl @Inject constructor(
    private val languagesDao: LanguagesDao,
    private val syncHelper: SyncHelper,
    private val dispatcherProvider: DispatcherProvider,
) : LanguagesRepository {
    override fun get(query: String?): Flow<Result<List<Language>>> {
        return (if (query.isNullOrEmpty()) {
            languagesDao.get().onStart { syncHelper.syncLanguages() }
        } else {
            languagesDao.search("%$query%")
        }).map { entities ->
            Result.success(
                entities.map { entity ->
                    Language(
                        code = entity.code,
                        name = entity.name,
                        nativeName = entity.nativeName.takeUnless { it.isEmpty() } ?: getNativeLanguageName(entity.code, entity.name)
                    )
                }
            )
        }
            .flowOn(dispatcherProvider.io)
            .catch {
                Timber.e(it)
                emit(Result.failure(it))
            }
    }

    private fun getNativeLanguageName(languageCode: String, languageName: String): String {
        val loc = Locale(languageCode)
        val name = loc.getDisplayLanguage(loc).takeUnless { it == languageCode } ?: languageName
        return name.replaceFirstChar { it.uppercase() }
    }
}
