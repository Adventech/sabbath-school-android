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

package ss.lessons.impl

import app.ss.models.OfflineState
import app.ss.models.SSDay
import app.ss.storage.db.entity.LessonEntity
import app.ss.storage.db.entity.QuarterlyEntity
import app.ss.storage.db.entity.QuarterlyInfoEntity
import app.ss.storage.db.entity.ReadEntity
import app.ss.storage.test.FakeQuarterliesDao
import app.ss.storage.test.FakeReadsDao
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import ss.foundation.coroutines.test.TestDispatcherProvider

class ContentSyncProviderImplTest {

    private val fakeQuarterliesDao = FakeQuarterliesDao()
    private val fakeReadsDao = FakeReadsDao()

    private val underTest = ContentSyncProviderImpl(
        quarterliesDao = fakeQuarterliesDao,
        readsDao = fakeReadsDao,
        dispatcherProvider = TestDispatcherProvider(),
    )

    private val quarterlyEntity = QuarterlyEntity(
        index = "index",
        id = "id",
        title = "Title",
        description = "Description",
        introduction = null,
        human_date = "date",
        start_date = "date",
        end_date = "date",
        cover = "",
        splash = null,
        path = "",
        full_path = "",
        lang = "",
        color_primary = "",
        color_primary_dark = "",
        quarterly_name = "",
        quarterly_group = null,
        features = emptyList(),
        credits = emptyList(),
        offlineState = OfflineState.NONE
    )
    private val lessonEntity = LessonEntity(
        index = "index",
        quarter = "",
        title = "Title",
        start_date = "date",
        end_date = "date",
        cover = "",
        id = "id",
        path = "path",
        full_path = "",
        pdfOnly = false,
    )
    private val day = SSDay(
        title = "Title",
        index = "index"
    )

    @Test
    fun `syncQuarterlies - empty lessons marked NONE`() = runTest {
        fakeQuarterliesDao.infoEntitiesForSync = listOf(
            QuarterlyInfoEntity(
                quarterly = quarterlyEntity.copy(
                    offlineState = OfflineState.PARTIAL
                ),
                lessons = emptyList()
            )
        )
        underTest.syncQuarterlies()

        fakeQuarterliesDao.lastUpdatedQuarterly!!.offlineState shouldBeEqualTo OfflineState.NONE
    }

    @Test
    fun `syncQuarterlies - empty reads marked PARTIAL`() = runTest {
        fakeQuarterliesDao.infoEntitiesForSync = listOf(
            QuarterlyInfoEntity(
                quarterly = quarterlyEntity,
                lessons = listOf(
                    lessonEntity.copy(days = listOf(day))
                )
            )
        )

        underTest.syncQuarterlies()

        fakeQuarterliesDao.lastUpdatedQuarterly!!.offlineState shouldBeEqualTo OfflineState.PARTIAL
    }

    @Test
    fun `syncQuarterlies - has all reads marked COMPLETE`() = runTest {
        fakeQuarterliesDao.infoEntitiesForSync = listOf(
            QuarterlyInfoEntity(
                quarterly = quarterlyEntity,
                lessons = listOf(
                    lessonEntity.copy(days = listOf(day))
                )
            )
        )
        fakeReadsDao.addRead(
            ReadEntity(
                index = day.index,
                id = day.id,
                date = day.date,
                title = day.title,
                content = "",
                bible = emptyList()
            )
        )

        underTest.syncQuarterlies()

        fakeQuarterliesDao.lastUpdatedQuarterly!!.offlineState shouldBeEqualTo OfflineState.COMPLETE
    }
}
