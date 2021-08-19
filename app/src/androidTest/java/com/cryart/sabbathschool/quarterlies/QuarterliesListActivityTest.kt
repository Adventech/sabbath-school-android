/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.quarterlies

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.lessons.data.model.QuarterlyGroup
import com.cryart.sabbathschool.actions.RecyclerViewItemCountAssertion.Companion.withItemCount
import com.cryart.sabbathschool.actions.withCollapsingToolbarTitle
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.ui.quarterlies.list.QuarterliesListActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QuarterliesListActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private var scenario: ActivityScenario<QuarterliesListActivity>? = null

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun verify_group_quarterlies_list() {
        val name = "Standard Adult"
        val group = QuarterlyGroup(name, 100)

        launch(group) {
            onView(withId(R.id.ss_toolbar_layout)).check(
                matches(
                    withCollapsingToolbarTitle(name)
                )
            )

            onView(withId(R.id.ss_quarterlies_list)).check(withItemCount(20))
        }
    }

    @Test
    fun verify_empty_state_shown() {
        val group = QuarterlyGroup("Invalid", 100)

        launch(group) {
            onView(withId(R.id.ss_quarterlies_list)).check(withItemCount(0))
        }
    }

    private fun launch(
        group: QuarterlyGroup,
        block: (ActivityScenario<QuarterliesListActivity>) -> Unit
    ) {
        val intent = QuarterliesListActivity.launchIntent(
            ApplicationProvider.getApplicationContext(),
            group
        )
        scenario = ActivityScenario.launch<QuarterliesListActivity>(intent).also {
            block(it)
        }
    }
}
