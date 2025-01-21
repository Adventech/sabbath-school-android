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

package ss.lessons.impl

import app.ss.models.OfflineState
import app.ss.models.SSLessonInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.ContentSyncProvider
import ss.lessons.api.SSLessonsApi
import ss.lessons.api.helper.SyncHelper
import ss.lessons.impl.ext.toEntity
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.libraries.storage.api.dao.ReadsDao
import ss.libraries.storage.api.entity.LessonEntity
import ss.libraries.storage.api.entity.QuarterlyEntity
import ss.workers.api.WorkScheduler
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ContentSyncProviderImpl @Inject constructor(
    private val quarterliesDao: QuarterliesDao,
    private val readsDao: ReadsDao,
    private val lessonsDao: LessonsDao,
    private val lessonsApi: SSLessonsApi,
    private val syncHelper: SyncHelper,
    private val workScheduler: WorkScheduler,
    private val dispatcherProvider: DispatcherProvider,
) : ContentSyncProvider {

    override suspend fun syncQuarterlies(): Result<Unit> {
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
        return Result.success(Unit)
    }

    private suspend fun markQuarterly(entity: QuarterlyEntity, state: OfflineState) {
        if (entity.offlineState == state) return
        quarterliesDao.update(entity.copy(offlineState = state))
    }

    private suspend fun LessonEntity.hasReads(): Boolean = days.isNotEmpty() && days.all { readsDao.get(it.index) != null }
    private fun LessonEntity.hasPdfFiles(): Boolean = pdfs.isNotEmpty() && pdfs.all { false }

    override suspend fun syncQuarterly(index: String): Result<Unit> {
        val initialState = quarterliesDao.getOfflineState(index) ?: OfflineState.NONE
        try {
            val language = index.substringBefore('-')
            val id = index.substringAfter('-')

            quarterliesDao.setOfflineState(index, OfflineState.IN_PROGRESS)

            val covers = mutableSetOf<String>()

            val quarterlyInfo = syncHelper.syncQuarterlyInfo(index)
            quarterlyInfo?.let {
                for (lesson in quarterlyInfo.lessons) {
                    val lessonInfoResponse = lessonsApi.getLessonInfo(language, id, lesson.id)
                    lessonInfoResponse.body()?.downloadContent()
                    lessonInfoResponse.body()?.lesson?.cover?.let { covers.add(it) }
                }
            }

            workScheduler.preFetchImages(covers)

            quarterliesDao.setOfflineState(
                index,
                OfflineState.COMPLETE
            )
            return Result.success(Unit)
        } catch (ex: Throwable) {
            Timber.e(ex)
            quarterliesDao.setOfflineState(index, initialState)
            return Result.failure(ex)
        }
    }

    override suspend fun removeAllDownloads(): Result<Unit> {
        lessonsDao.deleteAll()
        readsDao.deleteAll()
        return syncQuarterlies()
    }

    private suspend fun SSLessonInfo.downloadContent() {
        lessonsDao.updateInfo(
            lesson.index,
            days,
            pdfs,
            lesson.title,
            lesson.cover,
            lesson.path,
            lesson.full_path,
            lesson.pdfOnly
        )

        if (lesson.pdfOnly) {

        } else {
            for (day in days) {
                val response = lessonsApi.getDayRead("${day.full_read_path}/index.json")
                response.body()?.let { readsDao.insertItem(it.toEntity()) }
            }
        }
    }

    override fun hasDownloads(): Flow<Boolean> = combine(lessonsDao.getAll(), readsDao.getAll()) { lessons, reads ->
        lessons.isNotEmpty() && reads.isNotEmpty()
    }.flowOn(dispatcherProvider.io)
}
