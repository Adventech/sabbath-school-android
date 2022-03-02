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

import app.ss.models.media.SSAudio
import app.ss.storage.db.dao.AudioDao
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test

class MediaRepositoryImplTest {

    private val dispatcherProvider = TestDispatcherProvider()

    private val mockAudioDataSource: AudioDataSource = mockk()
    private val mockVideoDataSource: VideoDataSource = mockk()
    private val mockAudioDao: AudioDao = mockk()

    private lateinit var repository: MediaRepository

    @Before
    fun setup() {
        repository = MediaRepositoryImpl(
            audioDataSource = mockAudioDataSource,
            videoDataSource = mockVideoDataSource,
            audioDao = mockAudioDao,
            dispatcherProvider = dispatcherProvider
        )
    }

    @Test
    fun `should filter audios by targetIndex`() = runTest {
        val lessonIndex = "en-2021-03-09"

        val result = listOf(
            buildAudio(id = "1", targetIndex = "en-2021-03-09-01"),
            buildAudio(id = "2", targetIndex = "en-2021-03-09-02"),
            buildAudio(id = "3", targetIndex = "en-2021-04-03-01")
        )

        coEvery { mockAudioDataSource.get(AudioDataSource.Request("en-2021-03-09")) }
            .returns(Resource.success(result))
        every { mockAudioDao.insertAll(any()) }.returns(Unit)

        val response = repository.getAudio(lessonIndex)

        response.isSuccessFul shouldBeEqualTo true
        response.data?.size ?: 0 shouldBeEqualTo 2
    }

    private fun buildAudio(
        id: String = "id",
        targetIndex: String
    ): SSAudio = SSAudio(
        id = id,
        image = "",
        imageRatio = "",
        artist = "",
        src = "",
        target = "",
        targetIndex = targetIndex,
        title = ""
    )
}
