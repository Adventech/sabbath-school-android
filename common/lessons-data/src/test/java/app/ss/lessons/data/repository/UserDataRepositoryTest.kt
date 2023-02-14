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

package app.ss.lessons.data.repository

import app.cash.turbine.test
import app.ss.lessons.data.api.SSLessonsApi
import app.ss.lessons.data.model.api.ReadComments
import app.ss.lessons.data.model.api.ReadHighlights
import app.ss.lessons.data.repository.user.UserDataRepository
import app.ss.lessons.data.repository.user.UserDataRepositoryImpl
import app.ss.models.SSComment
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import app.ss.storage.db.dao.PdfAnnotationsDao
import app.ss.storage.db.dao.ReadCommentsDao
import app.ss.storage.db.dao.ReadHighlightsDao
import app.ss.storage.db.entity.ReadCommentsEntity
import app.ss.storage.db.entity.ReadHighlightsEntity
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.test.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import ss.prefs.api.SSPrefs

class UserDataRepositoryTest {

    private val lessonsApi: SSLessonsApi = mockk()
    private val readHighlightsDao: ReadHighlightsDao = mockk()
    private val readCommentsDao: ReadCommentsDao = mockk()
    private val pdfAnnotationsDao: PdfAnnotationsDao = mockk()
    private val ssPrefs: SSPrefs = mockk()
    private val connectivityHelper: ConnectivityHelper = mockk()

    private lateinit var repository: UserDataRepository

    @Before
    fun setup() {
        repository = UserDataRepositoryImpl(
            lessonsApi = lessonsApi,
            readHighlightsDao = readHighlightsDao,
            readCommentsDao = readCommentsDao,
            pdfAnnotationsDao = pdfAnnotationsDao,
            ssPrefs = ssPrefs,
            dispatcherProvider = TestDispatcherProvider(),
            connectivityHelper = connectivityHelper
        )
    }

    @Test
    fun getHighlights() = runTest {
        val index = "index-1-2-3"
        val dbFlow = MutableSharedFlow<ReadHighlightsEntity?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { readHighlightsDao.getFlow(index) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(false)

        repository.getHighlights(index).test {
            dbFlow.emit(null)

            awaitItem() shouldBeEqualTo Result.success(SSReadHighlights(index))
        }
    }

    @Test
    fun `get highlights map from db entity`() = runTest {
        val index = "index-1-2-3"
        val dbFlow = MutableSharedFlow<ReadHighlightsEntity?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { readHighlightsDao.getFlow(index) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(false)

        repository.getHighlights(index).test {
            dbFlow.emit(ReadHighlightsEntity(index, "1234"))

            awaitItem() shouldBeEqualTo Result.success(SSReadHighlights(index, "1234"))
        }
    }

    @Test
    fun `get highlights sync onStart`() = runTest {
        val index = "index-1-2-3"
        val dbFlow = MutableSharedFlow<ReadHighlightsEntity?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { readHighlightsDao.getFlow(index) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(true)
        coEvery { lessonsApi.getHighlights(index) }.returns(Response.success(ReadHighlights(index, "", 1L)))
        coEvery { readHighlightsDao.insertItem(ReadHighlightsEntity(index, "")) }.returns(Unit)

        repository.getHighlights(index).test {
            dbFlow.emit(null)

            awaitItem() shouldBeEqualTo Result.success(SSReadHighlights(index))

            coVerify { lessonsApi.getHighlights(index) }
            coVerify { readHighlightsDao.insertItem(ReadHighlightsEntity(index, "")) }
        }
    }

    @Test
    fun `save highlights`() = runTest {
        val highlights = SSReadHighlights("index", "123")
        val entity = ReadHighlightsEntity(highlights.readIndex, highlights.highlights)
        coEvery { readHighlightsDao.insertItem(entity) }.returns(Unit)
        coEvery { lessonsApi.uploadHighlights(highlights) }.returns(Response.success(null))
        every { connectivityHelper.isConnected() }.returns(true)

        repository.saveHighlights(highlights)

        coVerify { readHighlightsDao.insertItem(entity) }
        coVerify { lessonsApi.uploadHighlights(highlights) }
    }

    @Test
    fun getComments() = runTest {
        val index = "index-1-2-3"
        val dbFlow = MutableSharedFlow<ReadCommentsEntity?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { readCommentsDao.getFlow(index) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(false)

        repository.getComments(index).test {
            dbFlow.emit(null)

            awaitItem() shouldBeEqualTo Result.success(SSReadComments(index, emptyList()))
        }
    }

    @Test
    fun `get comments map from db entity`() = runTest {
        val index = "index-1-2-3"
        val dbFlow = MutableSharedFlow<ReadCommentsEntity?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { readCommentsDao.getFlow(index) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(false)

        repository.getComments(index).test {
            dbFlow.emit(ReadCommentsEntity(index, listOf(SSComment("123"))))

            awaitItem() shouldBeEqualTo Result.success(SSReadComments(index, listOf(SSComment("123"))))
        }
    }

    @Test
    fun `get comments sync onStart`() = runTest {
        val index = "index-1-2-3"
        val dbFlow = MutableSharedFlow<ReadCommentsEntity?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { readCommentsDao.getFlow(index) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(true)
        coEvery { lessonsApi.getComments(index) }.returns(Response.success(ReadComments(index, emptyList(), 1L)))
        coEvery { readCommentsDao.insertItem(ReadCommentsEntity(index, emptyList())) }.returns(Unit)

        repository.getComments(index).test {
            dbFlow.emit(null)

            awaitItem() shouldBeEqualTo Result.success(SSReadComments(index, emptyList()))

            coVerify { lessonsApi.getComments(index) }
            coVerify { readCommentsDao.insertItem(ReadCommentsEntity(index, emptyList())) }
        }
    }

    @Test
    fun `save comments`() = runTest {
        val comments = SSReadComments("index", listOf(SSComment("123")))
        val entity = ReadCommentsEntity(comments.readIndex, comments.comments)
        coEvery { readCommentsDao.insertItem(entity) }.returns(Unit)
        coEvery { lessonsApi.uploadComments(comments) }.returns(Response.success(null))
        every { connectivityHelper.isConnected() }.returns(true)

        repository.saveComments(comments)

        coVerify { readCommentsDao.insertItem(entity) }
        coVerify { lessonsApi.uploadComments(comments) }
    }


    @Test
    fun clear() = runTest {
        coEvery { readHighlightsDao.clear() }.returns(Unit)
        coEvery { readCommentsDao.clear() }.returns(Unit)
        coEvery { pdfAnnotationsDao.clear() }.returns(Unit)
        coEvery { ssPrefs.clear() }.returns(Unit)

        repository.clear()

        coVerify {
            readHighlightsDao.clear()
            readCommentsDao.clear()
            pdfAnnotationsDao.clear()
        }
        verify { ssPrefs.clear() }
    }
}
