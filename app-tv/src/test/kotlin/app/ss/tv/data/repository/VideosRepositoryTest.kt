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

import app.cash.turbine.test
import app.ss.storage.test.FakeLanguagesDao
import app.ss.storage.test.FakeVideoClipsDao
import app.ss.storage.test.FakeVideoInfoDao
import app.ss.tv.data.infoModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import retrofit2.Response
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.lessons.model.SSLanguage
import ss.lessons.test.FakeMediaApi
import ss.libraries.storage.api.entity.LanguageEntity
import ss.libraries.storage.api.entity.VideoInfoEntity

/** Tests for [VideosRepositoryImpl]. */
class VideosRepositoryTest {

    private val languagesFlow = MutableStateFlow<List<LanguageEntity>>(emptyList())
    private val videoInfoFlow = MutableStateFlow<List<VideoInfoEntity>>(emptyList())

    private val fakeMediaApi = FakeMediaApi()
    private val fakeLanguagesDao = FakeLanguagesDao(languagesFlow = languagesFlow)
    private val fakeVideoClipsDao = FakeVideoClipsDao()
    private val fakeVideoInfoDao = FakeVideoInfoDao(videoInfoFlow = videoInfoFlow)

    private val underTest: VideosRepository = VideosRepositoryImpl(
        mediaApi = fakeMediaApi,
        dispatcherProvider = TestDispatcherProvider(),
        languagesDao = fakeLanguagesDao,
        videoClipsDao = fakeVideoClipsDao,
        videoInfoDao = fakeVideoInfoDao,
    )

    @Test
    fun `getVideos - error response - return empty`() = runTest {
        fakeMediaApi.addVideoResponse("en", Response.error(404, "{}".toResponseBody()))

        underTest.getVideos().test {
            val result = awaitItem()

            result.isSuccess shouldBeEqualTo true
            result.getOrNull() shouldBeEqualTo emptyList()
        }
    }

    @Test
    fun `getVideos - from cache`() = runTest {
        underTest.getVideos().test {
            awaitItem()

            videoInfoFlow.emit(listOf(infoModel.toEntity("en")))

            val result = awaitItem()

            result.isSuccess shouldBeEqualTo true
            result.getOrNull() shouldBeEqualTo listOf(infoModel)
        }
    }

    @Test
    fun `getVideos - from network`() = runTest {
        fakeMediaApi.addVideoResponse("en", Response.success(listOf(infoModel)))

        underTest.getVideos().test {
            val result = awaitItem()

            result.isSuccess shouldBeEqualTo true
            result.getOrNull() shouldBeEqualTo listOf(infoModel)
        }
    }

    @Test
    fun `getLanguages - from cache`() = runTest {
        underTest.getLanguages().test {
            var result = awaitItem()
            result.getOrNull() shouldBeEqualTo emptyList()

            languagesFlow.emit(listOf(LanguageEntity("en", "English", "English")))

            result = awaitItem()

            result.isSuccess shouldBeEqualTo true
            result.getOrNull() shouldBeEqualTo listOf(SSLanguage("en", "English"))
        }
    }

    @Test
    fun `getLanguages - from network`() = runTest {
        fakeMediaApi.addLanguage("en")

        underTest.getLanguages().test {
            val result = awaitItem()

            result.isSuccess shouldBeEqualTo true
            result.getOrNull() shouldBeEqualTo listOf(SSLanguage("en", "English"))
        }
    }
}
