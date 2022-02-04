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

package app.ss.lessons.data.repository.mediator

import app.cash.turbine.test
import app.ss.lessons.data.api.SSQuarterliesApi
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.storage.db.dao.QuarterliesDao
import app.ss.storage.db.entity.QuarterlyEntity
import com.cryart.sabbathschool.test.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class QuarterliesDataSourceTest {

    private val mockQuarterliesDao: QuarterliesDao = mockk()
    private val mockQuarterliesApi: SSQuarterliesApi = mockk()

    private val dispatcherProvider = TestDispatcherProvider()

    private lateinit var dataSource: QuarterliesDataSource

    private val entity = QuarterlyEntity(
        id = "id", title = "title", description = "desc",
        introduction = null,
        human_date = "date", start_date = "date", end_date = "date",
        cover = "cover", splash = null, index = "index", path = "path",
        full_path = "full_path", lang = "en",
        color_primary = "color_primary", color_primary_dark = "color_primary_dark",
        quarterly_name = "name", quarterly_group = null, features = emptyList(),
        credits = emptyList()
    )
    private val quarterly = SSQuarterly(
        "id", "title", "desc", null, "date",
        "date", "date",
        "cover", null, "index", "path", "full_path", "en",
        "color_primary", "color_primary_dark", "name",
        null, emptyList(), emptyList()
    )

    @Before
    fun setup() {
        dataSource = QuarterliesDataSource(
            dispatcherProvider = dispatcherProvider,
            quarterliesDao = mockQuarterliesDao,
            quarterliesApi = mockQuarterliesApi
        )
    }

    @Test
    fun `should emit data from local then remote`() = runTest {
        every { mockQuarterliesDao.get("en") }.returns(
            listOf(entity.copy(title = "local"))
        )
        every {
            mockQuarterliesDao.insertAll(
                listOf(entity.copy(title = "remote"))
            )
        }.returns(Unit)
        coEvery { mockQuarterliesApi.getQuarterlies("en") }.returns(
            Response.success(
                listOf(quarterly.copy(title = "remote"))
            )
        )

        dataSource.getAsFlow(
            QuarterliesDataSource.Request("en")
        ).test {
            awaitItem().data shouldBeEqualTo listOf(quarterly.copy(title = "local"))
            awaitItem().data shouldBeEqualTo listOf(quarterly.copy(title = "remote"))

            awaitComplete()
            verify {
                mockQuarterliesDao.insertAll(
                    listOf(entity.copy(title = "remote"))
                )
            }
        }
    }

    @Test
    fun `should return loading response if local data is empty`() = runTest {
        every { mockQuarterliesDao.get("en") }.returns(emptyList())

        every {
            mockQuarterliesDao.insertAll(
                listOf(entity.copy(title = "remote"))
            )
        }.returns(Unit)
        coEvery { mockQuarterliesApi.getQuarterlies("en") }.returns(
            Response.success(
                listOf(quarterly.copy(title = "remote"))
            )
        )

        dataSource.getAsFlow(
            QuarterliesDataSource.Request("en")
        ).test {
            awaitItem().data shouldBeEqualTo listOf(quarterly.copy(title = "remote"))

            awaitComplete()
        }
    }

    @Test
    fun `should filter api response by quarterly group`() = runTest {
        val group = QuarterlyGroup("kids", 3)

        every { mockQuarterliesDao.get("en", group) }.returns(emptyList())
        every { mockQuarterliesDao.insertAll(any()) }.returns(Unit)

        coEvery { mockQuarterliesApi.getQuarterlies("en") }.returns(
            Response.success(
                listOf(quarterly, quarterly.copy(quarterly_group = group), quarterly)
            )
        )

        dataSource.getAsFlow(
            QuarterliesDataSource.Request("en", group)
        ).test {
            awaitItem().data shouldBeEqualTo listOf(quarterly.copy(quarterly_group = group))

            awaitComplete()
        }
    }
}
