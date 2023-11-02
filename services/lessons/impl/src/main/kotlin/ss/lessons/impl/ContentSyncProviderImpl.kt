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
import app.ss.models.SSLessonInfo
import app.ss.models.SSQuarterlyInfo
import app.ss.storage.db.dao.LessonsDao
import app.ss.storage.db.dao.QuarterliesDao
import app.ss.storage.db.dao.ReadsDao
import app.ss.storage.db.entity.LessonEntity
import app.ss.storage.db.entity.QuarterlyEntity
import ss.lessons.api.ContentSyncProvider
import ss.lessons.api.PdfReader
import ss.lessons.api.SSLessonsApi
import ss.lessons.api.SSQuarterliesApi
import ss.lessons.impl.ext.toEntity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ContentSyncProviderImpl @Inject constructor(
    private val quarterliesDao: QuarterliesDao,
    private val readsDao: ReadsDao,
    private val lessonsDao: LessonsDao,
    private val pdfReader: PdfReader,
    private val quarterliesApi: SSQuarterliesApi,
    private val lessonsApi: SSLessonsApi,
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
    private fun LessonEntity.hasPdfFiles(): Boolean = pdfs.isNotEmpty() && pdfs.all { pdfReader.isDownloaded(it) }

    override suspend fun syncQuarterly(index: String): Result<Unit> = try {
        val language = index.substringBefore('-')
        val id = index.substringAfter('-')

        quarterliesDao.setOfflineState(index, OfflineState.IN_PROGRESS)

        val response = quarterliesApi.getQuarterlyInfo(language, id)
        if (!response.isSuccessful || response.body() == null) {
            Result.failure(Throwable(response.errorBody()?.string()))
        } else {
            response.body()?.let { quarterlyInfo ->
                saveQuarterlyInfo(quarterlyInfo)
                for (lesson in quarterlyInfo.lessons) {
                    val lessonInfoResponse = lessonsApi.getLessonInfo(language, quarterlyInfo.quarterly.id, lesson.id)
                    lessonInfoResponse.body()?.downloadContent()
                }
            }

            syncQuarterlies()
        }
    } catch (ex: Throwable) {
        Timber.e(ex)
        syncQuarterlies()
        Result.failure(ex)
    }

    private suspend fun saveQuarterlyInfo(info: SSQuarterlyInfo) {
        info.lessons.forEach { lesson ->
            lessonsDao.get(lesson.index)?.let { entity ->
                lessonsDao.update(lesson.toEntity(entity.days, entity.pdfs))
            } ?: run { lessonsDao.insertItem(lesson.toEntity()) }
        }
        val state = quarterliesDao.getOfflineState(info.quarterly.index) ?: OfflineState.NONE
        quarterliesDao.update(info.quarterly.toEntity(state))
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
            pdfReader.downloadFiles(this.pdfs)
        } else {
            for (day in days) {
                val response = lessonsApi.getDayRead("${day.full_read_path}/index.json")
                response.body()?.let { readsDao.insertItem(it.toEntity()) }
            }
        }
    }
}
