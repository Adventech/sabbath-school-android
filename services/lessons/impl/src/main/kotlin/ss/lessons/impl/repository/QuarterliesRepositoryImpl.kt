/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

import app.ss.models.LessonIntroModel
import app.ss.models.PublishingInfo
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.models.SSQuarterlyInfo
import app.ss.storage.db.dao.QuarterliesDao
import app.ss.storage.db.entity.QuarterlyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.lessons.api.repository.QuarterliesRepositoryV2
import ss.lessons.impl.ext.toModel
import ss.lessons.impl.helper.SyncHelper
import ss.prefs.api.SSPrefs
import timber.log.Timber
import javax.inject.Inject

internal class QuarterliesRepositoryImpl @Inject constructor(
    private val quarterliesDao: QuarterliesDao,
    private val ssPrefs: SSPrefs,
    private val syncHelper: SyncHelper,
    private val dispatcherProvider: DispatcherProvider,
) : QuarterliesRepositoryV2, Scopable by ioScopable(dispatcherProvider) {

    override fun getQuarterly(index: String): Flow<Result<SSQuarterlyInfo>> = quarterliesDao
        .getInfoFlow(index)
        .filterNotNull()
        .map { entity ->
            Result.success(
                SSQuarterlyInfo(
                    quarterly = entity.quarterly.toModel(),
                    lessons = entity.lessons.map { it.toModel() }
                )
            )
        }
        .onStart { syncHelper.syncQuarterly(index) }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Result.failure(it))
        }

    override fun getQuarterlies(
        languageCode: String?,
        group: QuarterlyGroup?
    ): Flow<Result<List<SSQuarterly>>> = quarterliesDbFlow(languageCode, group)
        .map { entities -> Result.success(entities.map { it.toModel() }) }
        .onStart {
            val language = languageCode ?: ssPrefs.getLanguageCode()
            syncHelper.syncQuarterlies(language)
        }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Result.failure(it))
        }

    private fun quarterliesDbFlow(languageCode: String?, group: QuarterlyGroup?): Flow<List<QuarterlyEntity>> {
        val code = languageCode ?: ssPrefs.getLanguageCode()
        return group?.let { quarterliesDao.getFlow(code, it) } ?: quarterliesDao.getFlow(code)
    }

    override fun getPublishingInfo(languageCode: String?): Flow<Result<PublishingInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun getIntro(index: String): Result<LessonIntroModel?> {
        TODO("Not yet implemented")
    }

}
