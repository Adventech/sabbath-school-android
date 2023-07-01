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

package app.ss.lessons.data.repository

import app.cash.turbine.test
import app.ss.lessons.data.repository.user.UserDataRepository
import app.ss.lessons.data.repository.user.UserDataRepositoryImpl
import app.ss.models.PdfAnnotations
import app.ss.models.SSComment
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import app.ss.storage.db.dao.PdfAnnotationsDao
import app.ss.storage.db.dao.ReadCommentsDao
import app.ss.storage.db.dao.ReadHighlightsDao
import app.ss.storage.db.entity.PdfAnnotationsEntity
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
import ss.lessons.api.SSLessonsApi
import ss.lessons.model.ReadComments
import ss.lessons.model.ReadHighlights
import ss.lessons.model.request.UploadPdfAnnotationsRequest
import ss.misc.DeviceHelper
import ss.prefs.api.SSPrefs

class UserDataRepositoryTest {

    private val lessonsApi: SSLessonsApi = mockk()
    private val readHighlightsDao: ReadHighlightsDao = mockk()
    private val readCommentsDao: ReadCommentsDao = mockk()
    private val pdfAnnotationsDao: PdfAnnotationsDao = mockk()
    private val ssPrefs: SSPrefs = mockk()
    private val connectivityHelper: ConnectivityHelper = mockk()
    private val deviceHelper: DeviceHelper = mockk()

    private lateinit var repository: UserDataRepository

    private val defaultTimestamp = 1676421200L // February 15, 2023 12:33:20 AM

    @Before
    fun setup() {
        every { deviceHelper.nowEpochMilli() }.returns(defaultTimestamp)

        repository = UserDataRepositoryImpl(
            lessonsApi = lessonsApi,
            readHighlightsDao = readHighlightsDao,
            readCommentsDao = readCommentsDao,
            pdfAnnotationsDao = pdfAnnotationsDao,
            ssPrefs = ssPrefs,
            dispatcherProvider = TestDispatcherProvider(),
            connectivityHelper = connectivityHelper,
            deviceHelper = deviceHelper
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
            dbFlow.emit(ReadHighlightsEntity(index, "1234", defaultTimestamp))

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
        every { readHighlightsDao.get(index) }.returns(null)
        coEvery { readHighlightsDao.insertItem(ReadHighlightsEntity(index, "", 1L)) }.returns(Unit)

        repository.getHighlights(index).test {
            dbFlow.emit(null)

            awaitItem() shouldBeEqualTo Result.success(SSReadHighlights(index))

            coVerify { lessonsApi.getHighlights(index) }
            coVerify { readHighlightsDao.insertItem(ReadHighlightsEntity(index, "", 1L)) }
        }
    }

    @Test
    fun `get highlights sync onStart - upload cached`() = runTest {
        val index = "index-1-2-3"
        val timestamp = 1676896200L // February 20, 2023 12:30:00 PM
        val dbFlow = MutableSharedFlow<ReadHighlightsEntity?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { readHighlightsDao.getFlow(index) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(true)
        coEvery { lessonsApi.getHighlights(index) }.returns(Response.success(ReadHighlights(index, "", defaultTimestamp)))
        every { readHighlightsDao.get(index) }.returns(ReadHighlightsEntity(index, "cached", timestamp))

        repository.getHighlights(index).test {
            dbFlow.emit(null)

            awaitItem() shouldBeEqualTo Result.success(SSReadHighlights(index))

            coVerify { lessonsApi.uploadHighlights(SSReadHighlights(index, "cached")) }
        }
    }

    @Test
    fun `save highlights`() = runTest {
        val highlights = SSReadHighlights("index", "123")
        val entity = ReadHighlightsEntity(highlights.readIndex, highlights.highlights, defaultTimestamp)
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
            dbFlow.emit(ReadCommentsEntity(index, listOf(SSComment("123")), defaultTimestamp))

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
        every { readCommentsDao.get(index) }.returns(null)
        coEvery { readCommentsDao.insertItem(ReadCommentsEntity(index, emptyList(), 1L)) }.returns(Unit)

        repository.getComments(index).test {
            dbFlow.emit(null)

            awaitItem() shouldBeEqualTo Result.success(SSReadComments(index, emptyList()))

            coVerify { lessonsApi.getComments(index) }
            coVerify { readCommentsDao.insertItem(ReadCommentsEntity(index, emptyList(), 1L)) }
        }
    }

    @Test
    fun `get comments sync onStart - upload cached`() = runTest {
        val index = "index-1-2-3"
        val timestamp = 1676896200L // February 20, 2023 12:30:00 PM
        val dbFlow = MutableSharedFlow<ReadCommentsEntity?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { readCommentsDao.getFlow(index) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(true)
        coEvery { lessonsApi.getComments(index) }.returns(Response.success(ReadComments(index, listOf(SSComment("remote")), defaultTimestamp)))
        every { readCommentsDao.get(index) }.returns(ReadCommentsEntity(index, listOf(SSComment("cached")), timestamp))

        repository.getComments(index).test {
            dbFlow.emit(null)

            awaitItem() shouldBeEqualTo Result.success(SSReadComments(index, emptyList()))

            coVerify { lessonsApi.uploadComments(SSReadComments(index, listOf(SSComment("cached")))) }
        }
    }

    @Test
    fun `save comments`() = runTest {
        val comments = SSReadComments("index", listOf(SSComment("123")))
        val entity = ReadCommentsEntity(comments.readIndex, comments.comments, defaultTimestamp)
        coEvery { readCommentsDao.insertItem(entity) }.returns(Unit)
        coEvery { lessonsApi.uploadComments(comments) }.returns(Response.success(null))
        every { connectivityHelper.isConnected() }.returns(true)

        repository.saveComments(comments)

        coVerify { readCommentsDao.insertItem(entity) }
        coVerify { lessonsApi.uploadComments(comments) }
    }

    @Test
    fun getAnnotations() = runTest {
        val index = "index"
        val pdfId = "pdf"
        val dbFlow = MutableSharedFlow<List<PdfAnnotationsEntity>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { pdfAnnotationsDao.getFlow("$index-$pdfId") }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(false)

        repository.getAnnotations(index, pdfId).test {
            dbFlow.emit(emptyList())

            awaitItem() shouldBeEqualTo Result.success(emptyList())
        }
    }

    @Test
    fun `get annotations map from db entity`() = runTest {
        val index = "index"
        val pdfId = "pdf"
        val pageIndex = "$index-$pdfId"
        val dbFlow = MutableSharedFlow<List<PdfAnnotationsEntity>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        every { pdfAnnotationsDao.getFlow(pageIndex) }.returns(dbFlow)
        every { connectivityHelper.isConnected() }.returns(false)

        repository.getAnnotations(index, pdfId).test {
            dbFlow.emit(
                listOf(
                    PdfAnnotationsEntity(
                        index = pageIndex,
                        pdfIndex = "$pageIndex-0",
                        pageIndex = 0,
                        annotations = listOf("123"),
                        timestamp = defaultTimestamp
                    )
                )
            )

            awaitItem() shouldBeEqualTo Result.success(
                listOf(
                    PdfAnnotations(
                        pageIndex = 0,
                        annotations = listOf("123")
                    )
                )
            )
        }
    }

    @Test
    fun `save annotations`() = runTest {
        val index = "index"
        val pdfId = "pdf"
        val pageIndex = "$index-$pdfId"
        val annotations = listOf(PdfAnnotations(pageIndex = 0, annotations = listOf("123")))
        val entity = PdfAnnotationsEntity(
            index = "$pageIndex-0",
            pdfIndex = pageIndex,
            pageIndex = 0,
            annotations = listOf("123"),
            timestamp = defaultTimestamp
        )
        coEvery { pdfAnnotationsDao.insertAll(listOf(entity)) }.returns(Unit)
        coEvery { lessonsApi.uploadAnnotations(index, pdfId, UploadPdfAnnotationsRequest(annotations)) }
            .returns(Response.success(null))
        every { connectivityHelper.isConnected() }.returns(true)

        repository.saveAnnotations(index, pdfId, annotations)

        coVerify {
            pdfAnnotationsDao.insertAll(listOf(entity))
        }

        coVerify {
            lessonsApi.uploadAnnotations(
                lessonIndex = index,
                pdfId = pdfId,
                request = UploadPdfAnnotationsRequest(annotations)
            )
        }
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
