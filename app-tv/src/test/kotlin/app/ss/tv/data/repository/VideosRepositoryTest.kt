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

import app.ss.tv.data.infoModel
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import retrofit2.Response
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.lessons.test.FakeMediaApi

/** Tests for [VideosRepositoryImpl]. */
class VideosRepositoryTest {

    private val mediaApi = FakeMediaApi()

    private val underTest: VideosRepository = VideosRepositoryImpl(
        mediaApi,
        TestDispatcherProvider()
    )

    @Test
    fun `getVideos - default error`() = runTest {
        val result = underTest.getVideos()

        result.isFailure shouldBeEqualTo true
    }

    @Test
    fun `getVideos - error response`() = runTest {
        mediaApi.addVideoResponse("en", Response.error(404, "{}".toResponseBody()))

        val result = underTest.getVideos()

        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo emptyList()
    }

    @Test
    fun `getVideos - success response`() = runTest {
        mediaApi.addVideoResponse("en", Response.success(listOf(infoModel)))

        val result = underTest.getVideos()

        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo listOf(infoModel)
    }
}
