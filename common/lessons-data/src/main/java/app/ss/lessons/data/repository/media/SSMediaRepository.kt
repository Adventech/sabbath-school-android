/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.lessons.data.repository.media

import app.ss.lessons.data.api.SSMediaApi
import app.ss.lessons.data.model.api.SSAudio
import app.ss.lessons.data.model.api.SSVideosInfo
import app.ss.lessons.data.model.api.request.SSMediaRequest
import app.ss.lessons.data.model.media.AudioFile
import app.ss.lessons.data.model.media.toAudio
import app.ss.storage.db.dao.AudioDao
import app.ss.storage.db.entity.AudioFileEntity
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.withContext
import timber.log.Timber

interface SSMediaRepository {
    suspend fun getAudio(lessonIndex: String): Resource<List<SSAudio>>
    suspend fun findAudioFile(id: String): AudioFile?
    suspend fun updateDuration(id: String, duration: Long)
    suspend fun getPlayList(lessonIndex: String): List<AudioFile>

    suspend fun getVideo(lessonIndex: String): Resource<List<SSVideosInfo>>
}

internal class SSMediaRepositoryImpl(
    private val mediaApi: SSMediaApi,
    private val audioDao: AudioDao,
    private val schedulerProvider: SchedulerProvider
) : SSMediaRepository {

    override suspend fun getAudio(
        lessonIndex: String,
    ): Resource<List<SSAudio>> {

        return try {
            val request = lessonIndex.toMediaRequest() ?: return Resource.error(Throwable("Invalid Index"))
            val response = mediaApi.getAudio(request.language, request.quarterlyId)

            val audios = response.body() ?: emptyList()
            if (audios.isNotEmpty()) {
                withContext(schedulerProvider.io) {
                    audioDao.insertAll(
                        audios.map { it.toEntity() }
                    )
                }
            }

            val lessonAudios = audios.filter { it.targetIndex.startsWith(lessonIndex) }

            Resource.success(lessonAudios)
        } catch (ex: Throwable) {
            Resource.error(ex)
        }
    }

    override suspend fun findAudioFile(id: String): AudioFile? = withContext(schedulerProvider.io) {
        audioDao.findBy(id)?.toAudio()
    }

    override suspend fun updateDuration(id: String, duration: Long) = withContext(schedulerProvider.io) {
        Timber.i("Updating duration...$duration")
        audioDao.update(duration, id)
    }

    override suspend fun getPlayList(lessonIndex: String): List<AudioFile> = withContext(schedulerProvider.io) {
        audioDao.searchBy("%$lessonIndex%").map {
            it.toAudio()
        }
    }

    override suspend fun getVideo(lessonIndex: String): Resource<List<SSVideosInfo>> {
        return try {
            val request = lessonIndex.toMediaRequest() ?: return Resource.error(Throwable("Invalid Index"))
            val response = mediaApi.getVideo(request.language, request.quarterlyId)

            val videos = response.body() ?: emptyList()
            if (videos.isNotEmpty()) {
                // cache
            }

            Resource.success(videos)
        } catch (ex: Exception) {
            Timber.e(ex)
            Resource.error(ex)
        }
    }
}

private fun String.toMediaRequest(): SSMediaRequest? {
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
    title = title,
)
