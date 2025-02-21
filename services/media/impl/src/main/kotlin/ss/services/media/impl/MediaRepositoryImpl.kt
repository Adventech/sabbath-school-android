/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.services.media.impl

import android.net.Uri
import app.ss.models.media.AudioFile
import app.ss.models.media.SSAudio
import app.ss.models.media.SSVideosInfo
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.defaultScopable
import ss.lessons.api.SSMediaApi
import ss.lessons.model.request.SSMediaRequest
import ss.libraries.media.api.MediaRepository
import ss.libraries.storage.api.dao.AudioDao
import ss.libraries.storage.api.dao.VideoInfoDao
import ss.libraries.storage.api.entity.AudioFileEntity
import ss.libraries.storage.api.entity.VideoInfoEntity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MediaRepositoryImpl @Inject constructor(
    private val audioDao: AudioDao,
    private val videoInfoDao: VideoInfoDao,
    private val mediaApi: SSMediaApi,
    private val connectivityHelper: ConnectivityHelper,
    private val dispatcherProvider: DispatcherProvider
) : MediaRepository, Scopable by defaultScopable(dispatcherProvider) {

    private val exceptionLogger = CoroutineExceptionHandler { _, exception -> Timber.e(exception) }

    override fun getAudio(lessonIndex: String): Flow<List<SSAudio>> = audioDao
        .getAsFlow("$lessonIndex%")
        .map { entities -> entities.map { it.toSSAudio() } }
        .onStart { syncAudio(lessonIndex) }
        .flowOn(dispatcherProvider.io)
        .catch { Timber.e(it) }

    private fun syncAudio(lessonIndex: String) = scope.launch(exceptionLogger) {
        val resource = lessonIndex.toMediaRequest()?.let {
            safeApiCall(connectivityHelper) {
                mediaApi.getAudio(it.language, it.quarterlyId)
            }
        } ?: return@launch
        when (resource) {
            is NetworkResource.Failure -> {
                Timber.e("Failed to fetch audio for $lessonIndex => ${resource.errorBody}")
            }
            is NetworkResource.Success -> {
                val lessonAudios = resource.value.body()
                    ?.filter { it.targetIndex.startsWith(lessonIndex) }
                    ?.map { it.toEntity() } ?: emptyList()

                withContext(dispatcherProvider.io) {
                    audioDao.delete()
                    audioDao.insertAll(lessonAudios)
                }
            }
        }
    }

    override suspend fun findAudioFile(id: String): AudioFile? = withContext(dispatcherProvider.io) {
        audioDao.findBy(id)?.toAudio()
    }

    override suspend fun getPlayList(lessonIndex: String): List<AudioFile> = withContext(dispatcherProvider.io) {
        audioDao.getBy("$lessonIndex%").map {
            it.toAudio()
        }
    }

    override fun getVideo(lessonIndex: String): Flow<List<SSVideosInfo>> = videoInfoDao
        .getAsFlow(lessonIndex)
        .map { entities -> entities.map { it.toModel() } }
        .flowOn(dispatcherProvider.io)
        .catch { Timber.e(it) }
}

internal fun String.toMediaRequest(): SSMediaRequest? {
    if (this.isEmpty()) {
        return null
    }
    val langId = this.substringBeforeLast('-')
    val lang = langId.substringBefore('-')
    val id = langId.substringAfter('-')
    return SSMediaRequest(lang, id)
}

fun SSAudio.toEntity(): AudioFileEntity = AudioFileEntity(
    id = id,
    artist = artist,
    image = image,
    imageRatio = imageRatio,
    src = src,
    target = target,
    targetIndex = targetIndex,
    title = title
)

fun AudioFileEntity.toAudio(): AudioFile = AudioFile(
    id = id,
    artist = artist,
    image = image,
    imageRatio = imageRatio,
    source = Uri.parse(src),
    target = target,
    targetIndex = targetIndex,
    title = title
)

fun AudioFileEntity.toSSAudio(): SSAudio = SSAudio(
    id = id,
    artist = artist,
    image = image,
    imageRatio = imageRatio,
    src = src,
    target = target,
    targetIndex = targetIndex,
    title = title
)

private fun VideoInfoEntity.toModel(): SSVideosInfo = SSVideosInfo(
    id = id,
    artist = artist,
    clips = clips,
    lessonIndex = lessonIndex
)

