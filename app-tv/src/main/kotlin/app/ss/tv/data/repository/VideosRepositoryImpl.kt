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

package app.ss.tv.data.repository

import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.SSMediaApi
import ss.lessons.model.SSLanguage
import ss.lessons.model.VideosInfoModel
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideosRepositoryImpl @Inject constructor(
    private val mediaApi: SSMediaApi,
    private val dispatcherProvider: DispatcherProvider
) : VideosRepository {

    private var videoCache: MutableMap<String, List<VideosInfoModel>> = mutableMapOf()
    private var languagesCache: List<SSLanguage> = emptyList()

    override suspend fun getVideos(language: String): Result<List<VideosInfoModel>> = withContext(dispatcherProvider.default) {
        return@withContext videoCache[language]?.let { Result.success(it) } ?: run {
            try {
                val response = mediaApi.getLatestVideo(language)
                val videos = response.body() ?: emptyList()
                if (videos.isNotEmpty()) {
                    videoCache[language] = videos
                }

                Result.success(videos)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                Result.failure(throwable)
            }
        }
    }

    override suspend fun getLanguages(): Result<List<SSLanguage>> = withContext(dispatcherProvider.default) {
        return@withContext languagesCache.takeIf { it.isNotEmpty() }?.let {
            Result.success(it)
        } ?: run {
            try {
                val response = mediaApi.getVideoLanguages()
                val languages = response.body()?.toModel() ?: emptyList()
                if (languages.isNotEmpty()) {
                    languagesCache = languages
                }
                Result.success(languages)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                Result.failure(throwable)
            }
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
