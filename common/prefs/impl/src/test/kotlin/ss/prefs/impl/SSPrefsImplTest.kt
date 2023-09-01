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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.prefs.impl

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.prefs.model.SSReadingDisplayOptions

@RunWith(AndroidJUnit4::class)
class SSPrefsImplTest {

    private val mockDataStore: DataStore<Preferences> = mockk(relaxed = true)
    private val mockSharedPreferences: SharedPreferences = mockk()

    private lateinit var impl: SSPrefsImpl

    @Before
    fun setup() {
        impl = SSPrefsImpl(
            mockDataStore,
            mockSharedPreferences,
            mockk(),
            TestDispatcherProvider()
        )
    }

    @Test
    fun `default value returned for empty preferences`() = runTest {
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
    fun `default value returned when exception is thrown during synchronous read`() = runTest {
        every { mockDataStore.data }.answers { error("IO error") }

        impl.getDisplayOptions { options ->
            options shouldBeEqualTo SSReadingDisplayOptions("default", "medium", "lato")
        }
    }

    @Test
    fun `clear sharedPreferences`() = runTest {
        val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
        every { mockSharedPreferences.edit() }.returns(mockEditor)

        impl.clear()

        verify {
            mockSharedPreferences.edit()
            mockEditor.clear()
            mockEditor.apply()
        }
    }
}
