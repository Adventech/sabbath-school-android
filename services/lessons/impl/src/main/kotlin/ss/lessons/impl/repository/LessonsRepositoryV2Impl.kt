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

import app.ss.models.SSDay
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.lessons.api.SSLessonsApi
import ss.lessons.api.repository.LessonsRepositoryV2
import ss.lessons.impl.ext.toEntity
import ss.lessons.impl.ext.toInfoModel
import ss.lessons.impl.ext.toModel
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.ReadsDao
import ss.libraries.storage.api.entity.LessonEntity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LessonsRepositoryV2Impl @Inject constructor(
    private val lessonsApi: SSLessonsApi,
    private val lessonsDao: LessonsDao,
    private val readsDao: ReadsDao,
    private val connectivityHelper: ConnectivityHelper,
    private val dispatcherProvider: DispatcherProvider,
) : LessonsRepositoryV2, Scopable by ioScopable(dispatcherProvider) {

    private val exceptionLogger = CoroutineExceptionHandler { _, exception -> Timber.e(exception) }

    override fun getLessonInfo(
        lessonIndex: String
    ): Flow<Result<SSLessonInfo>> = lessonsDao
        .getAsFlow(lessonIndex)
        .filterNotNull()
        .map { Result.success(it.toInfoModel()) }
        .onStart { syncLessonInfo(lessonIndex) }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Result.failure(it))
        }

    override suspend fun getLessonInfoResult(lessonIndex: String, path: String): Result<SSLessonInfo> {
        val cached = withContext(dispatcherProvider.io) {
            lessonsDao.get(lessonIndex)?.takeUnless { it.days.isEmpty() }
        }

        return if (cached == null) {
            fetchLessonInfo(path)?.let {
                Result.success(it.toInfoModel())
            } ?: Result.failure(Throwable("Failed to fetch Lesson Info: $lessonIndex"))
        } else {
            Result.success(cached.toInfoModel())
        }
    }

    private fun syncLessonInfo(lessonIndex: String) = scope.launch(exceptionLogger) {
        val entity = lessonsDao.get(lessonIndex) ?: return@launch
        val updated = fetchLessonInfo(entity.path) ?: return@launch
        lessonsDao.insertItem(updated)
    }

    private suspend fun fetchLessonInfo(path: String): LessonEntity? {
        val parts = path.split("/")
        require(parts.size >= 5) { "Invalid path format $path" }

        val language = parts[0]
        val quarterlyId = parts[2]

        return when (val response = safeApiCall(connectivityHelper) { lessonsApi.getLessonInfo(path) }) {
            is NetworkResource.Failure -> {
                Timber.e("Failed to fetch Lesson Info: isNetwork=${response.isNetworkError}, ${response.errorBody}")
                null
            }

            is NetworkResource.Success -> response.value.body()?.let { info ->
                val lesson = lessonsDao.get(info.lesson.index)
                val quarterlyIndex = lesson?.quarter ?: "$language-$quarterlyId"
                val order = lesson?.order ?: 0

                info.lesson.toEntity(quarterlyIndex, order, info.days, info.pdfs)
            }
        }
    }

    override fun getDayRead(day: SSDay): Flow<Result<SSRead>> = readsDao.getAsFlow(day.index)
        .filterNotNull()
        .map { Result.success(it.toModel()) }
        .onStart { syncRead(day) }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Result.failure(it))
        }

    private fun syncRead(day: SSDay) = scope.launch(exceptionLogger) {
        when (val response = safeApiCall(connectivityHelper) { lessonsApi.getDayRead("${day.full_read_path}/index.json") }) {
            is NetworkResource.Failure -> {
                Timber.e("Failed to fetch Day Read: isNetwork=${response.isNetworkError}, ${response.errorBody}")
            }

            is NetworkResource.Success -> response.value.body()?.let { info -> readsDao.insertItem(info.toEntity()) }
        }
    }
}
