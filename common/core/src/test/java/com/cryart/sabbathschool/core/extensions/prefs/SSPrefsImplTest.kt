package com.cryart.sabbathschool.core.extensions.prefs

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.test.coroutines.CoroutineTestRule
import com.cryart.sabbathschool.test.coroutines.runBlockingTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.ExperimentalTime

@ExperimentalTime
@RunWith(AndroidJUnit4::class)
class SSPrefsImplTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val mockDataStore: DataStore<Preferences> = mockk(relaxed = true)
    private val mockSharedPreferences: SharedPreferences = mockk()

    private lateinit var impl: SSPrefsImpl

    @Before
    fun setup() {
        impl = SSPrefsImpl(
            ApplicationProvider.getApplicationContext(),
            mockDataStore,
            mockSharedPreferences,
            CoroutineScope(coroutinesTestRule.testDispatcher)
        )
    }

    @Test
    fun `default value returned for empty preferences`() = coroutinesTestRule.runBlockingTest {
        val testFlow = flow {
            emit(emptyPreferences())
            emit(emptyPreferences())
        }
        every { mockDataStore.data }.returns(testFlow)
        impl.displayOptionsFlow().test {
            awaitItem() shouldBeEqualTo SSReadingDisplayOptions("default", "medium", "lato")
            awaitComplete()
        }
    }

    @Test
    fun `default value returned when exception is thrown during synchronous read`() = coroutinesTestRule.runBlockingTest {
        every { mockDataStore.data }.answers { error("IO error") }
        impl.getDisplayOptions { options ->
            options shouldBeEqualTo SSReadingDisplayOptions("default", "medium", "lato")
        }
    }

    @Test
    fun `clear sharedPreferences`() = coroutinesTestRule.runBlockingTest {
        val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
        every { mockSharedPreferences.edit() }.returns(mockEditor)
        impl.clear()
        verify {
            mockSharedPreferences.edit()
            mockEditor.clear()
        }
    }
}
