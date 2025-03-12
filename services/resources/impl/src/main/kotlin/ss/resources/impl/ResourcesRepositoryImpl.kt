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
import app.ss.models.AudioAux
import app.ss.models.PDFAux
import app.ss.models.VideoAux
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.feed.FeedType
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
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
import ss.libraries.storage.api.dao.AudioDao
import ss.libraries.storage.api.dao.BibleVersionDao
import ss.libraries.storage.api.dao.DocumentsDao
import ss.libraries.storage.api.dao.FeedDao
import ss.libraries.storage.api.dao.FeedGroupDao
import ss.libraries.storage.api.dao.FontFilesDao
import ss.libraries.storage.api.dao.LanguagesDao
import ss.libraries.storage.api.dao.ResourcesDao
import ss.libraries.storage.api.dao.SegmentsDao
import ss.libraries.storage.api.dao.UserInputDao
import ss.libraries.storage.api.dao.VideoInfoDao
import ss.prefs.api.SSPrefs
import ss.resources.api.ResourcesRepository
import ss.resources.impl.ext.toEntity
import ss.resources.impl.ext.toModel
import ss.resources.impl.sync.SyncHelper
import ss.resources.model.FeedModel
import ss.resources.model.FontModel
import ss.resources.model.LanguageModel
import timber.log.Timber
import java.io.File
import javax.inject.Inject

private const val FONTS_DIR = "fonts"

internal class ResourcesRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val resourcesApi: ResourcesApi,
    private val audioDao: AudioDao,
    private val documentsDao: DocumentsDao,
    private val feedDao: FeedDao,
    private val feedGroupDao: FeedGroupDao,
    private val videoInfoDao: VideoInfoDao,
    private val fontFilesDao: FontFilesDao,
    private val languagesDao: LanguagesDao,
    private val segmentsDao: SegmentsDao,
    private val userInputDao: UserInputDao,
    private val resourcesDao: ResourcesDao,
    private val bibleVersionDao: BibleVersionDao,
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

    override fun feed(type: FeedType): Flow<FeedModel> {
        val language = ssPrefs.get().getLanguageCode()

        return feedDao.get(language, type)
            .filterNotNull()
            .map { entity -> entity.toModel() }
            .onStart { syncHelper.syncFeed(language, type) }
            .flowOn(dispatcherProvider.io)
            .catch { Timber.e(it) }
    }

    override fun feedGroup(id: String, type: FeedType): Flow<FeedGroup> {
        val language = ssPrefs.get().getLanguageCode()
        return feedGroupDao
            .get(id)
            .filterNotNull()
            .map { it.toModel() }
            .onStart { syncHelper.syncFeedGroup(id, language, type) }
            .flowOn(dispatcherProvider.io)
            .catch { Timber.e(it) }
    }

    override fun resource(index: String): Flow<Resource> = resourcesDao
        .get(index)
        .filterNotNull()
        .map { it.toModel() }
        .onStart { syncHelper.syncResource(index) }
        .flowOn(dispatcherProvider.io)
        .catch { Timber.e(it) }

    override fun document(index: String): Flow<ResourceDocument> = documentsDao
        .get(index)
        .filterNotNull()
        .map { it.toModel() }
        .onStart { syncHelper.syncDocument(index) }
        .flowOn(dispatcherProvider.io)
        .catch { Timber.e(it) }

    override fun documentInput(documentId: String): Flow<List<UserInput>> {
        return userInputDao.getDocumentInput(documentId)
            .map { entities -> entities.map { it.input } }
            .onStart { syncHelper.syncUserInput(documentId) }
            .flowOn(dispatcherProvider.io)
            .catch {
                Timber.e(it)
                emit(emptyList())
            }
    }

    override fun saveDocumentInput(documentId: String, input: UserInputRequest) = syncHelper.saveUserInput(documentId, input)

    override fun segment(id: String, index: String): Flow<Segment> = segmentsDao.get(id)
        .filterNotNull()
        .map { it.toModel() }
        .onStart { syncHelper.syncSegment(index) }
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
                        val audio = it.filter { it.target.startsWith(documentIndex) }
                        withContext(dispatcherProvider.io) {
                            audioDao.delete()
                            audioDao.insertAll(audio.map { it.toEntity() })
                        }
                        Result.success(audio)
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
                        val videos = it
                        val entities = videos.mapIndexed { index, video -> video.toEntity("$resourceIndex-$index", documentIndex) }
                        withContext(dispatcherProvider.io) {
                            videoInfoDao.delete()
                            videoInfoDao.insertAll(entities)
                        }
                        Result.success(videos)
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

    override fun fontFile(fontName: String): Flow<FontModel?> {
        return fontFilesDao.get(fontName)
            .filterNotNull()
            .map { entity ->
                FontModel(
                    file = File(appContext.filesDir, "$FONTS_DIR/${entity.fileName}"),
                    attributes = entity.attributes
                )
            }
            .filter { it.file.exists() }
            .flowOn(dispatcherProvider.io)
    }

    override fun bibleVersion(): Flow<String?> =
        bibleVersionDao.get(ssPrefs.get().getLanguageCode())
            .map { it?.version }
            .flowOn(dispatcherProvider.io)

    override fun saveBibleVersion(version: String) =
        syncHelper.saveBibleVersion(ssPrefs.get().getLanguageCode(), version)
}
