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

import android.net.Uri
import app.ss.models.media.SSAudio
import app.ss.models.media.SSVideosInfo
import app.ss.lessons.data.model.api.request.SSMediaRequest
import app.ss.models.media.AudioFile
import app.ss.storage.db.dao.AudioDao
import app.ss.storage.db.entity.AudioFileEntity
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface MediaRepository {
    suspend fun getAudio(lessonIndex: String): Resource<List<SSAudio>>
    suspend fun findAudioFile(id: String): AudioFile?
    suspend fun updateDuration(id: String, duration: Long)
    suspend fun getPlayList(lessonIndex: String): List<AudioFile>
    suspend fun getVideo(lessonIndex: String): Resource<List<SSVideosInfo>>
}

@Singleton
internal class MediaRepositoryImpl @Inject constructor(
    private val audioDataSource: AudioDataSource,
    private val videoDataSource: VideoDataSource,
    private val audioDao: AudioDao,
    private val dispatcherProvider: DispatcherProvider
) : MediaRepository {

    override suspend fun getAudio(lessonIndex: String): Resource<List<SSAudio>> = withContext(dispatcherProvider.io) {
        audioDataSource.get(AudioDataSource.Request(lessonIndex))
    }

    override suspend fun findAudioFile(id: String): AudioFile? = withContext(dispatcherProvider.io) {
        audioDao.findBy(id)?.toAudio()
    }

    override suspend fun updateDuration(id: String, duration: Long) = withContext(dispatcherProvider.io) {
        audioDao.update(duration, id)
    }

    override suspend fun getPlayList(lessonIndex: String): List<AudioFile> = withContext(dispatcherProvider.io) {
        audioDao.getBy("$lessonIndex%").map {
            it.toAudio()
        }
    }

    override suspend fun getVideo(lessonIndex: String): Resource<List<SSVideosInfo>> = withContext(dispatcherProvider.io) {
        videoDataSource.get(VideoDataSource.Request(lessonIndex))
    }
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
