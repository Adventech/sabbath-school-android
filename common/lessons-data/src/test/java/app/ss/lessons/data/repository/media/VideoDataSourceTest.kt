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

package app.ss.lessons.data.repository.media

import app.ss.models.media.SSVideo
import app.ss.models.media.SSVideosInfo
import app.ss.storage.db.dao.VideoInfoDao
import app.ss.storage.db.entity.VideoInfoEntity
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.lessons.api.SSMediaApi
import ss.lessons.model.VideosInfoModel

private const val LESSON_INDEX = "en-2022-03-09"

class VideoDataSourceTest {

    private val mockMediaApi: SSMediaApi = mockk()
    private val mockVideoDao: VideoInfoDao = mockk()
    private val mockConnectivityHelper: ConnectivityHelper = mockk()

    private lateinit var dataSource: VideoDataSource

    private val video = SSVideo(
        artist = "Artist",
        id = "id",
        src = "",
        target = "",
        targetIndex = "",
        thumbnail = "",
        title = "Title"
    )
    private val videosInfoModel = VideosInfoModel(
        artist = "Artist",
        clips = listOf(video)
    )

    @Before
    fun setup() {
        every { mockConnectivityHelper.isConnected() }.returns(true)

        dataSource = VideoDataSource(
            mediaApi = mockMediaApi,
            videoInfoDao = mockVideoDao,
            dispatcherProvider = TestDispatcherProvider(),
            connectivityHelper = mockConnectivityHelper
        )
    }

    @Test
    fun `should clear cache before updating`() = runTest {
        val quarterlyIndex = "en-2022-03"
        val entities = listOf(
            VideoInfoEntity(
                id = "$quarterlyIndex-0",
                lessonIndex = quarterlyIndex,
                artist = video.artist,
                clips = listOf(video)
            )
        )
        coEvery { mockMediaApi.getVideo("en", "2022-03") }
            .returns(Response.success(listOf(videosInfoModel)))
        coEvery { mockVideoDao.delete(quarterlyIndex) }
            .returns(Unit)
        coEvery { mockVideoDao.insertAll(entities) }
            .returns(Unit)

        val resource = dataSource.get(VideoDataSource.Request(LESSON_INDEX))

        resource.data shouldBeEqualTo listOf(
            SSVideosInfo(
                id = "$quarterlyIndex-0",
                artist = video.artist,
                clips = videosInfoModel.clips,
                lessonIndex = quarterlyIndex
            )
        )

        coVerifyOrder {
            mockVideoDao.delete(quarterlyIndex)
            mockVideoDao.insertAll(entities)
        }
    }
}
