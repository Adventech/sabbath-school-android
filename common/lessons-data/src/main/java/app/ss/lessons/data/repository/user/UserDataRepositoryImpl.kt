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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.lessons.data.repository.user

import app.ss.lessons.data.api.SSLessonsApi
import app.ss.models.PdfAnnotations
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import app.ss.storage.db.dao.PdfAnnotationsDao
import app.ss.storage.db.dao.ReadCommentsDao
import app.ss.storage.db.dao.ReadHighlightsDao
import app.ss.storage.db.entity.ReadCommentsEntity
import app.ss.storage.db.entity.ReadHighlightsEntity
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.prefs.api.SSPrefs
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UserDataRepositoryImpl @Inject constructor(
    private val lessonsApi: SSLessonsApi,
    private val readHighlightsDao: ReadHighlightsDao,
    private val readCommentsDao: ReadCommentsDao,
    private val pdfAnnotationsDao: PdfAnnotationsDao,
    private val ssPrefs: SSPrefs,
    private val dispatcherProvider: DispatcherProvider,
    private val connectivityHelper: ConnectivityHelper,
) : UserDataRepository, CoroutineScope by CoroutineScope(dispatcherProvider.default) {

    override fun getHighlights(readIndex: String): Flow<Result<SSReadHighlights>> = readHighlightsDao
        .getFlow(readIndex)
        .map { Result.success(SSReadHighlights(readIndex, it?.highlights ?: "")) }
        .onStart { syncHighlights(readIndex) }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Result.failure(it))
        }

    private fun syncHighlights(readIndex: String) = launch {
        val response = safeApiCall(connectivityHelper) { lessonsApi.getHighlights(readIndex) }
        if (response is NetworkResource.Success) {
            val highlights = response.value.body() ?: return@launch
            // val cached = withContext(dispatcherProvider.io) { readHighlightsDao.get(readIndex) }
            withContext(dispatcherProvider.io) {
                readHighlightsDao.insertItem(ReadHighlightsEntity(readIndex, highlights.highlights))
            }
        }
    }

    override fun saveHighlights(highlights: SSReadHighlights) {
        launch {
            withContext(dispatcherProvider.io) {
                readHighlightsDao.insertItem(
                    ReadHighlightsEntity(highlights.readIndex, highlights.highlights)
                )
            }

            safeApiCall(connectivityHelper) {
                lessonsApi.uploadHighlights(highlights)
            }
        }
    }

    override fun getComments(readIndex: String): Flow<Result<SSReadComments>> = readCommentsDao
        .getFlow(readIndex)
        .map { Result.success(SSReadComments(readIndex, it?.comments ?: emptyList())) }
        .onStart { syncComments(readIndex) }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Result.failure(it))
        }

    private fun syncComments(readIndex: String) = launch {
        val response = safeApiCall(connectivityHelper) { lessonsApi.getComments(readIndex) }
        if (response is NetworkResource.Success) {
            val comments = response.value.body() ?: return@launch
            withContext(dispatcherProvider.io) {
                readCommentsDao.insertItem(ReadCommentsEntity(readIndex, comments.comments))
            }
        }
    }

    override fun saveComments(comments: SSReadComments) {
        launch {
            withContext(dispatcherProvider.io) {
                readCommentsDao.insertItem(
                    ReadCommentsEntity(comments.readIndex, comments.comments)
                )
            }

            safeApiCall(connectivityHelper) {
                lessonsApi.uploadComments(comments)
            }
        }
    }

    override fun getAnnotations(lessonIndex: String, pdfId: String): Flow<Result<List<PdfAnnotations>>> {
        TODO("Not yet implemented")
    }

    override fun saveAnnotations(lessonIndex: String, pdfId: String, annotations: List<PdfAnnotations>) {
        TODO("Not yet implemented")
    }

    override suspend fun clear() {
        withContext(dispatcherProvider.io) {
            readHighlightsDao.clear()
            readCommentsDao.clear()
            pdfAnnotationsDao.clear()
            ssPrefs.clear()
        }
    }
}
