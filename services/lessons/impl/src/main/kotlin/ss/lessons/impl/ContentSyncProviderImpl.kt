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

package ss.lessons.impl

import app.ss.models.OfflineState
import app.ss.models.SSDay
import app.ss.storage.db.dao.QuarterliesDao
import app.ss.storage.db.dao.ReadsDao
import app.ss.storage.db.entity.QuarterlyEntity
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.lessons.api.ContentSyncProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ContentSyncProviderImpl @Inject constructor(
    private val quarterliesDao: QuarterliesDao,
    private val readsDao: ReadsDao,
    private val dispatcherProvider: DispatcherProvider
) : ContentSyncProvider, Scopable by ioScopable(dispatcherProvider) {

    override suspend fun syncQuarterlies(): Result<Unit> = withContext(dispatcherProvider.io) {
        quarterliesDao.getAllForSync().forEach { info ->
            val lessons = info.lessons
            if (lessons.isEmpty()) {
                markQuarterly(info.quarterly, OfflineState.NONE)
                return@forEach
            }

            val hasReads = lessons.all { lesson -> lesson.days.all { day -> day.hasReads() } }
            markQuarterly(
                info.quarterly,
                if (hasReads) OfflineState.COMPLETE else OfflineState.PARTIAL
            )
        }
        Result.success(Unit)
    }

    private suspend fun markQuarterly(entity: QuarterlyEntity, state: OfflineState) {
        if (entity.offlineState == state) return
        quarterliesDao.update(entity.copy(offlineState = state))
    }

    private suspend fun SSDay.hasReads(): Boolean = readsDao.get(index) != null

    override suspend fun syncQuarterly(index: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}
