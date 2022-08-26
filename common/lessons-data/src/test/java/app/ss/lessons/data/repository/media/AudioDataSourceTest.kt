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
import app.ss.models.media.SSAudio
import app.ss.storage.db.dao.AudioDao
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.test.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import retrofit2.Response

private const val LESSON_INDEX = "en-2022-03-09"

class AudioDataSourceTest {

    private val mockMediaApi: SSMediaApi = mockk()
    private val mockAudioDao: AudioDao = mockk()
    private val mockConnectivityHelper: ConnectivityHelper = mockk()

    private lateinit var dataSource: AudioDataSource

    private val ssAudio = SSAudio(
        id = "id",
        artist = "",
        image = "",
        imageRatio = "",
        src = "",
        target = LESSON_INDEX,
        targetIndex = "$LESSON_INDEX-1",
        title = "Audio title"
    )
    private val audioEntity = ssAudio.toEntity()

    @Before
    fun setup() {
        every { mockConnectivityHelper.isConnected() }.returns(true)

        dataSource = AudioDataSource(
            mediaApi = mockMediaApi,
            audioDao = mockAudioDao,
            dispatcherProvider = TestDispatcherProvider(),
            connectivityHelper = mockConnectivityHelper
        )
    }

    @Test
    fun `should clear cache before updating`() = runTest {
        coEvery { mockMediaApi.getAudio("en", "2022-03") }
            .returns(Response.success(listOf(ssAudio)))
        coEvery { mockAudioDao.delete(LESSON_INDEX) }
            .returns(Unit)
        every { mockAudioDao.insertAll(listOf(audioEntity)) }
            .returns(Unit)

        val resource = dataSource.get(AudioDataSource.Request(LESSON_INDEX))

        resource.data shouldBeEqualTo listOf(ssAudio)

        coVerifyOrder {
            mockAudioDao.delete(LESSON_INDEX)
            mockAudioDao.insertAll(listOf(audioEntity))
        }
    }
}
