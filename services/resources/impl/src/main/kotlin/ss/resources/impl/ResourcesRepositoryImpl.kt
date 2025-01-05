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

package ss.resources.impl

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.ss.models.AudioAux
import app.ss.models.PDFAux
import app.ss.models.VideoAux
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.feed.FeedType
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.Segment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.ResourcesApi
import ss.libraries.storage.api.dao.FontFilesDao
import ss.libraries.storage.api.dao.LanguagesDao
import ss.libraries.storage.api.dao.SegmentsDao
import ss.libraries.storage.api.entity.LanguageEntity
import ss.prefs.api.SSPrefs
import ss.resources.api.ResourcesRepository
import ss.resources.impl.ext.toEntity
import ss.resources.impl.ext.toModel
import ss.resources.impl.sync.SyncHelper
import ss.resources.impl.work.DownloadResourceWork
import ss.resources.model.FeedModel
import ss.resources.model.LanguageModel
import timber.log.Timber
import java.io.File
import javax.inject.Inject

internal class ResourcesRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val resourcesApi: ResourcesApi,
    private val fontFilesDao: FontFilesDao,
    private val languagesDao: LanguagesDao,
    private val segmentsDao: SegmentsDao,
    private val syncHelper: SyncHelper,
    private val dispatcherProvider: DispatcherProvider,
    private val connectivityHelper: ConnectivityHelper,
    private val ssPrefs: Lazy<SSPrefs>,
) : ResourcesRepository {

    override fun languages(query: String?): Flow<List<LanguageModel>> {
        return return (if (query.isNullOrEmpty()) {
            languagesDao.get().onStart { syncHelper.syncLanguages() }
        } else {
            languagesDao.search("%$query%")
        }).map { entities -> entities.map { it.toModel() } }
            .flowOn(dispatcherProvider.io)
            .catch {
                Timber.e(it)
                emit(emptyList())
            }
    }

    override fun language(code: String): Flow<LanguageModel> =
        languagesDao.get(code)
            .onStart { syncHelper.syncLanguages() }
            .filterNotNull()
            .map { entity -> entity.toModel() }
            .flowOn(dispatcherProvider.io)
            .catch { Timber.e(it) }

    private fun LanguageEntity.toModel() = LanguageModel(
        code = code,
        name = name,
        nativeName = nativeName,
        devo = devo,
        pm = pm,
        aij = aij,
        ss = ss,
    )

    override suspend fun feed(type: FeedType): Result<FeedModel> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) {
                resourcesApi.feed(
                    language = ssPrefs.get().getLanguageCode(),
                    type = type.name.lowercase()
                )
            }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch feed, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    resource.value.body()?.let {
                        Result.success(
                            FeedModel(title = it.title, it.groups)
                        )
                    } ?: Result.failure(Throwable("Failed to fetch feed, body is null"))
                }
            }
        }
    }

    override suspend fun feedGroup(id: String, type: FeedType): Result<FeedGroup> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) {
                resourcesApi.feedGroup(
                    language = ssPrefs.get().getLanguageCode(),
                    type = type.name.lowercase(),
                    groupId = id
                )
            }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch feed group, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    resource.value.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Throwable("Failed to fetch feed group, body is null"))
                }
            }
        }
    }

    override suspend fun resource(index: String): Result<Resource> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) {
                resourcesApi.resource(index)
            }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch Resource, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    downloadResource(index)

                    resource.value.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Throwable("Failed to fetch Resource, body is null"))
                }
            }
        }
    }

    private fun downloadResource(index: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<DownloadResourceWork>()
            .setConstraints(constraints)
            .setInputData(workDataOf(DownloadResourceWork.INDEX_KEY to index))
            .build()

        val workManager = WorkManager.getInstance(appContext)
        workManager.enqueueUniqueWork(
            DownloadResourceWork::class.java.simpleName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override suspend fun document(index: String): Result<ResourceDocument> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) {
                resourcesApi.document(index)
            }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch Document, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    resource.value.body()?.let {
                        withContext(dispatcherProvider.io) {
                            segmentsDao.insertAll(it.segments.orEmpty().map { it.toEntity() })
                        }
                        Result.success(it)
                    } ?: Result.failure(Throwable("Failed to fetch Document, body is null"))
                }
            }
        }
    }

    override fun segment(index: String): Flow<Segment> = segmentsDao.get(index)
        .filterNotNull()
        .map { it.toModel() }
        .flowOn(dispatcherProvider.io)
        .catch { Timber.e(it) }

    override suspend fun audio(resourceIndex: String, documentIndex: String): Result<List<AudioAux>> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) {
                resourcesApi.audio(resourceIndex)
            }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch Audio, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    resource.value.body()?.let {
                        Result.success(it.filter { it.target.startsWith(documentIndex) })
                    } ?: Result.failure(Throwable("Failed to fetch Audio, body is null"))
                }
            }
        }
    }

    override suspend fun video(resourceIndex: String, documentIndex: String): Result<List<VideoAux>> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) {
                resourcesApi.video(resourceIndex)
            }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch Videos, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    resource.value.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Throwable("Failed to fetch Videos, body is null"))
                }
            }
        }
    }

    override suspend fun pdf(resourceIndex: String, documentIndex: String): Result<List<PDFAux>> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) {
                resourcesApi.pdf(resourceIndex)
            }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch PDFs, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    resource.value.body()?.let {
                        Result.success(it.filter { it.target == documentIndex })
                    } ?: Result.failure(Throwable("Failed to fetch PDFs, body is null"))
                }
            }
        }
    }

    override suspend fun fontFile(name: String): Flow<File?> {
        return fontFilesDao.get(name)
            .filterNotNull()
            .map { File(appContext.filesDir, "fonts/${it.fileName}") }
            .filter { it.exists() }
            .flowOn(dispatcherProvider.io)
    }
}
