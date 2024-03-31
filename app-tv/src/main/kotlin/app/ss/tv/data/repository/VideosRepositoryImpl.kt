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

package app.ss.tv.data.repository

import androidx.annotation.VisibleForTesting
import app.ss.models.media.SSVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.SSMediaApi
import ss.lessons.model.SSLanguage
import ss.lessons.model.VideosInfoModel
import ss.libraries.storage.api.dao.LanguagesDao
import ss.libraries.storage.api.dao.VideoClipsDao
import ss.libraries.storage.api.dao.VideoInfoDao
import ss.libraries.storage.api.entity.LanguageEntity
import ss.libraries.storage.api.entity.VideoClipEntity
import ss.libraries.storage.api.entity.VideoInfoEntity
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideosRepositoryImpl @Inject constructor(
    private val mediaApi: SSMediaApi,
    private val dispatcherProvider: DispatcherProvider,
    private val languagesDao: LanguagesDao,
    private val videoClipsDao: VideoClipsDao,
    private val videoInfoDao: VideoInfoDao,
) : VideosRepository {

    override fun getVideos(language: String): Flow<Result<List<VideosInfoModel>>> = videoInfoDao
        .getAsFlow(language)
        .map { entities -> Result.success(entities.map { it.toModel() }) }
        .flowOn(dispatcherProvider.io)
        .catch { emit(Result.failure(it)) }
        .onStart { loadVideos(language) }

    private suspend fun loadVideos(language: String) = withContext(dispatcherProvider.default) {
        try {
            val response = mediaApi.getLatestVideo(language)
            val videos = response.body() ?: emptyList()
            if (videos.isNotEmpty()) {
                val entities = videos.map { it.toEntity(language) }
                withContext(dispatcherProvider.io) {
                    videoInfoDao.insertAll(entities)
                    videoClipsDao.insertAll(videos.flatMap { model -> model.clips.map { it.toEntity() } })
                }
            }
        } catch (throwable: Throwable) {
            Timber.e(throwable)
        }
    }

    override fun getLanguages(): Flow<Result<List<SSLanguage>>> = languagesDao
        .get()
        .map { entities -> Result.success(entities.map { SSLanguage(it.code, it.name) }) }
        .flowOn(dispatcherProvider.io)
        .catch { emit(Result.failure(it)) }
        .onStart { loadLanguages() }

    private suspend fun loadLanguages() = withContext(dispatcherProvider.default) {
        try {
            val response = mediaApi.getVideoLanguages()
            val languages = response.body()?.toModel() ?: emptyList()
            if (languages.isNotEmpty()) {
                withContext(dispatcherProvider.io) {
                    languagesDao.insertAll(languages.map { LanguageEntity(it.code, it.name, it.name) })
                }
            }
        } catch (throwable: Throwable) {
            Timber.e(throwable)
        }
    }

    private fun List<String>.toModel(): List<SSLanguage> {
        return map { code ->
            val loc = Locale(code)
            val name = loc.getDisplayLanguage(loc).takeUnless { it == code } ?: ""
            SSLanguage(
                code = code,
                name = name.replaceFirstChar { it.uppercase() }
            )
        }
    }
}

@VisibleForTesting
fun VideosInfoModel.toEntity(language: String): VideoInfoEntity = VideoInfoEntity(
    id = "$language-$artist",
    artist = artist,
    clips = clips,
    lessonIndex = language
)

private fun VideoInfoEntity.toModel(): VideosInfoModel = VideosInfoModel(
    artist = artist,
    clips = clips,
)

private fun SSVideo.toEntity() = VideoClipEntity(
    id = id,
    artist = artist,
    src = src,
    target = target,
    targetIndex = targetIndex,
    thumbnail = thumbnail,
    title = title,
)
