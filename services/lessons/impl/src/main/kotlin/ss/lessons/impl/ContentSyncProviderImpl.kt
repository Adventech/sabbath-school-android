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
import app.ss.storage.db.dao.QuarterliesDao
import app.ss.storage.db.dao.ReadsDao
import app.ss.storage.db.entity.LessonEntity
import app.ss.storage.db.entity.QuarterlyEntity
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.lessons.api.ContentSyncProvider
import ss.lessons.api.PdfReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ContentSyncProviderImpl @Inject constructor(
    private val quarterliesDao: QuarterliesDao,
    private val readsDao: ReadsDao,
    private val pdfReader: PdfReader,
    private val dispatcherProvider: DispatcherProvider,
) : ContentSyncProvider, Scopable by ioScopable(dispatcherProvider) {

    override suspend fun syncQuarterlies(): Result<Unit> = withContext(dispatcherProvider.io) {
        quarterliesDao.getAllForSync().forEach { info ->
            val lessons = info.lessons
            val state = when {
                lessons.isEmpty() -> OfflineState.NONE
                lessons.all { lesson ->
                    if (lesson.pdfOnly) {
                        lesson.hasPdfFiles()
                    } else {
                        lesson.hasReads()
                    }
                } -> OfflineState.COMPLETE

                else -> OfflineState.PARTIAL
            }
            markQuarterly(info.quarterly, state)
        }
        Result.success(Unit)
    }

    private suspend fun markQuarterly(entity: QuarterlyEntity, state: OfflineState) {
        if (entity.offlineState == state) return
        quarterliesDao.update(entity.copy(offlineState = state))
    }

    private suspend fun LessonEntity.hasReads(): Boolean = days.isNotEmpty() && days.all { readsDao.get(it.index) != null }
    private fun LessonEntity.hasPdfFiles(): Boolean = pdfs.isNotEmpty() && pdfs.all { pdfReader.isDownloaded(it) }

    override suspend fun syncQuarterly(index: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}
