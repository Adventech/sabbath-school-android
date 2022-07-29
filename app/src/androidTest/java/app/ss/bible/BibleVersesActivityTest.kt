package app.ss.bible

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryart.sabbathschool.test.di.mock.MockDataFactory
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BibleVersesActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private var scenario: ActivityScenario<BibleVersesActivity>? = null

    private val versions = MockDataFactory.versions

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun cleanUp() {
        scenario?.close()
    }

    @Test
    fun launchScreen() = launch {}

    private fun launch(block: (ActivityScenario<BibleVersesActivity>) -> Unit) {
        val intent = BibleVersesActivity.launchIntent(
            ApplicationProvider.getApplicationContext(),
            "1John14",
            "index"
        )
        scenario = ActivityScenario.launch<BibleVersesActivity>(intent).also {
            block(it)
        }
    }
}
