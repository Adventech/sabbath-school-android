package com.cryart.sabbathschool.bible

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.test.di.mock.MockDataFactory
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
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
    fun launchScreen() = launch {}

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
