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

package ss.lessons.impl.repository

import app.cash.turbine.test
import app.ss.models.SSDay
import app.ss.models.SSLesson
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.storage.test.FakeLessonsDao
import app.ss.storage.test.FakeReadsDao
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import ss.foundation.android.connectivity.FakeConnectivityHelper
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.lessons.api.repository.LessonsRepositoryV2
import ss.lessons.test.FakeLessonsApi
import ss.libraries.storage.api.entity.LessonEntity
import ss.libraries.storage.api.entity.ReadEntity

class LessonsRepositoryV2ImplTest {

    private val fakeLessonsApi = FakeLessonsApi()
    private val fakeLessonsDao = FakeLessonsDao()
    private val fakeReadsDao = FakeReadsDao()
    private val fakeConnectivityHelper = FakeConnectivityHelper(true)

    private val underTest: LessonsRepositoryV2 = LessonsRepositoryV2Impl(
        lessonsApi = fakeLessonsApi,
        lessonsDao = fakeLessonsDao,
        readsDao = fakeReadsDao,
        connectivityHelper = fakeConnectivityHelper,
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `getLessonInfo - emits from cache`() = runTest {
        val lessonIndex = "en-2021-03-09"
        val days = listOf(
            SSDay("Day 1", date = "03/04/2021", full_path = "path_1"),
            SSDay("Day 2", date = "04/04/2021", full_path = "path_2"),
            SSDay("Day 3", date = "05/04/2021", full_path = "path_3")
        )
        val lessonInfo = SSLessonInfo(
            lesson = SSLesson("Title", index = lessonIndex),
            days = days
        )

        underTest.getLessonInfo(lessonIndex).test {
            fakeLessonsDao.addLesson(
                LessonEntity(
                    index = lessonInfo.lesson.index,
                    quarter = lessonInfo.lesson.index,
                    title = lessonInfo.lesson.title,
                    start_date = "",
                    end_date = "",
                    cover = "",
                    id = "",
                    path = "",
                    full_path = "",
                    pdfOnly = false,
                    days = lessonInfo.days,
                )
            )

            awaitItem() shouldBeEqualTo Result.success(lessonInfo)
        }
    }

    @Test
    fun `getDayRead - emits from cache`() = runTest {
        val ssDay = SSDay(
            "Day 1",
            id = "1",
            index = "en-2021-03-09-01",
            date = "03/04/2021",
            full_path = "path_1"
        )
        val read = SSRead(
            id = ssDay.id,
            index = ssDay.index,
            date = ssDay.date
        )
        underTest.getDayRead(ssDay).test {
            fakeReadsDao.addRead(
                ReadEntity(
                    index = ssDay.index,
                    id = ssDay.id,
                    date = ssDay.date,
                    title = "",
                    content = "",
                    bible = emptyList()
                )
            )
            awaitItem() shouldBeEqualTo Result.success(read)
        }
    }
}
