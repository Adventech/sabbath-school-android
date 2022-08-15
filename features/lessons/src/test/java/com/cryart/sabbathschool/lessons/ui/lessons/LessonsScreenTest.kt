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

package com.cryart.sabbathschool.lessons.ui.lessons

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ss.design.compose.theme.SsTheme
import com.cryart.sabbathschool.test.di.MockModule
import com.cryart.sabbathschool.test.di.repository.FakeQuarterliesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("flaky")
class LessonsScreenTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5
    )

    private lateinit var repository: FakeQuarterliesRepository

    @Before
    fun setup() {
        repository = FakeQuarterliesRepository(
            mockData = MockModule.provideQuarterlyMockData(paparazzi.context)
        )
    }

    @Test
    fun lessons_screen() = runTest {
        launch(index = INDEX)
    }

    @Test
    fun lessons_screen_splash() = runTest {
        launch(index = INDEX_SPLASH)
    }

    private suspend fun launch(
        index: String
    ) {
        val quarterlyInfo = repository.getQuarterlyInfo(index).first().data!!

        val state = LessonsScreenState(
            isLoading = false,
            quarterlyInfo = QuarterlyInfoState.Success(quarterlyInfo)
        )
        paparazzi.snapshot {
            SsTheme {
                LessonsScreen(state = state)
            }
        }
    }

    companion object {
        private const val INDEX = "en-2021-02"
        private const val INDEX_SPLASH = "en-2021-03"
    }
}
