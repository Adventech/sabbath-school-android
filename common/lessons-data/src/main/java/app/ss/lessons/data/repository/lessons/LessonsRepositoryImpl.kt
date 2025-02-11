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

package app.ss.lessons.data.repository.lessons

import app.ss.models.SSDay
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.libraries.storage.api.dao.BibleVersionDao
import ss.libraries.storage.api.entity.BibleVersionEntity
import ss.prefs.api.SSPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LessonsRepositoryImpl @Inject constructor(
    private val ssPrefs: SSPrefs,
    private val lessonInfoDataSource: LessonInfoDataSource,
    private val readsDataSource: ReadsDataSource,
    private val bibleVersionDao: BibleVersionDao,
    private val dispatcherProvider: DispatcherProvider,
    private val readerArtifactHelper: ReaderArtifactHelper
) : LessonsRepository {

    override suspend fun getLessonInfo(lessonIndex: String, path: String, cached: Boolean): Resource<SSLessonInfo> {
        return if (cached) {
            withContext(dispatcherProvider.io) {
                lessonInfoDataSource.cache.getItem(LessonInfoDataSource.Request(lessonIndex, path))
            }
        } else {
            lessonInfoDataSource.getItem(LessonInfoDataSource.Request(lessonIndex, path))
        }
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> = withContext(dispatcherProvider.io) {
        readsDataSource.cache.getItem(
            ReadsDataSource.Request(dayIndex = dayIndex, fullPath = "")
        )
    }

    override suspend fun getDayRead(day: SSDay): Resource<SSRead> =
        readsDataSource.getItem(
            ReadsDataSource.Request(
                dayIndex = day.index,
                fullPath = day.full_read_path
            )
        )

    override fun checkReaderArtifact() {
        readerArtifactHelper.sync()
    }

    override suspend fun savePreferredBibleVersion(version: String) = withContext(dispatcherProvider.io) {
        bibleVersionDao.insertItem(BibleVersionEntity(ssPrefs.getLanguageCode(), version))
    }
}
