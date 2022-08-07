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

package com.cryart.sabbathschool.lessons.ui.quarterlies

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ss.design.compose.extensions.surface.ThemeSurface
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesGroupCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesListCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyList
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.QuarterliesGroup
import com.cryart.sabbathschool.test.di.MockModule
import com.cryart.sabbathschool.test.di.repository.FakeQuarterliesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

/**
 * Snapshot test for the [QuarterliesScreen].
 *
 * We can't test the entire screen combined
 * because of an internal bug in [androidx.compose.material3.Scaffold].
 *
 * Workaround is testing the [QuarterlyList] and [QuarterliesTopAppBar] separately.
 */
@Ignore("flaky")
class QuarterliesScreenTest {

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
    fun grouped_quarterlies() = runTest {
        val quarterlies = loadQuarterlies()

        paparazzi.snapshot {
            ThemeSurface {
                QuarterlyList(data = quarterlies)
            }
        }
    }

    @Test
    fun quarterly_group() = runTest {
        val quarterlies = loadQuarterlies(
            QuarterlyGroup("Standard Adult", 100)
        )

        paparazzi.snapshot {
            ThemeSurface {
                QuarterlyList(data = quarterlies)
            }
        }
    }

    @Test
    fun quarterlies_group_top_app_bar() {
        paparazzi.snapshot {
            ThemeSurface {
                QuarterliesTopAppBar(
                    title = "Sabbath School",
                    type = object : QuarterliesGroupCallback {
                        override fun onSeeAllClick(group: QuarterlyGroup) {}

                        override fun profileClick() {}

                        override fun filterLanguages() {}

                        override fun onReadClick(index: String) {}
                    }
                )
            }
        }
    }

    @Test
    fun quarterlies_list_top_app_bar() {
        paparazzi.snapshot {
            ThemeSurface {
                QuarterliesTopAppBar(
                    title = "Standard Adult",
                    type = object : QuarterliesListCallback {
                        override fun backNavClick() {}

                        override fun onReadClick(index: String) {}
                    }
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun loadQuarterlies(
        group: QuarterlyGroup? = null
    ): GroupedQuarterlies {
        val data = repository.getQuarterlies("en", group).first().data ?: emptyList()

        val grouped = data
            .groupBy { it.quarterly_group }
            .toSortedMap(compareBy { it?.order })

        val groupType = when {
            grouped.keys.size == 1 -> {
                GroupedQuarterlies.TypeList(grouped[grouped.firstKey()] ?: emptyList())
            }
            grouped.keys.size > 1 -> {
                val filtered = grouped.filterKeys { it != null } as Map<QuarterlyGroup, List<SSQuarterly>>
                if (filtered.keys.size > 1) {
                    val groups = filtered.map { map ->
                        QuarterliesGroup(map.key, map.value)
                    }
                    GroupedQuarterlies.TypeGroup(groups)
                } else {
                    GroupedQuarterlies.TypeList(filtered[filtered.keys.first()] ?: emptyList())
                }
            }
            else -> GroupedQuarterlies.Empty
        }

        return groupType
    }
}
