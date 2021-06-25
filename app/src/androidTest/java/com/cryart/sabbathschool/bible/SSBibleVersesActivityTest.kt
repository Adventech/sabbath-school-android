package com.cryart.sabbathschool.bible

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.test.di.repository.MockDataFactory
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.anything
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SSBibleVersesActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var ssPrefs: SSPrefs

    private var scenario: ActivityScenario<SSBibleVersesActivity>? = null

    private val versions = MockDataFactory.versions

    @Before
    fun setup() {
        hiltRule.inject()

        ssPrefs.setLastBibleUsed(versions.first())
    }

    @After
    fun cleanUp() {
        scenario?.close()
    }

    @Test
    fun toggleBibleVersions() = launch {
        onView(withText(versions[0])).check(matches(isDisplayed()))

        onView(withId(R.id.ss_reading_bible_version_list)).perform(click())

        onData(anything()).atPosition(1).perform(click())

        onView(withText(versions[1])).check(matches(isDisplayed()))

        onView(withId(R.id.ss_reading_bible_version_list)).perform(click())

        onData(anything()).atPosition(2).perform(click())

        onView(withText(versions[2])).check(matches(isDisplayed()))
    }

    @Test
    fun closeScreen() = launch {
        onView(withId(R.id.bibleCloseIV)).perform(click())

        scenario?.onActivity { activity ->
            assertTrue(activity.isFinishing)
        }
    }

    private fun launch(block: (ActivityScenario<SSBibleVersesActivity>) -> Unit) {
        val intent = SSBibleVersesActivity.launchIntent(
            ApplicationProvider.getApplicationContext(),
            "1John14",
            "index"
        )
        scenario = ActivityScenario.launch<SSBibleVersesActivity>(intent).also {
            block(it)
        }
    }
}
