/*
 * Copyright (c) 2020. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.ui.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.cryart.sabbathschool.test.coroutines.CoroutineTestRule
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val mockFirebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val mockDailyReminderManager: DailyReminderManager = mockk()
    private val mockSSPrefs: SSPrefs = mockk()

    private lateinit var viewModel: SplashViewModel

    @Before
    fun setUp() {
        every { mockDailyReminderManager.scheduleReminder() }.returns(Unit)
        every { mockSSPrefs.reminderEnabled() }.returns(true)
        every { mockSSPrefs.isReminderScheduled() }.returns(false)
        every { mockSSPrefs.isReadingLatestQuarterly() }.returns(false)

        viewModel = SplashViewModel(
            mockFirebaseAuth,
            mockSSPrefs,
            mockDailyReminderManager,
        )
    }

    @Test
    fun `should schedule reminder when user is signed in`() {
        every { mockFirebaseAuth.currentUser }.returns(mockk())

        SplashViewModel(
            mockFirebaseAuth,
            mockSSPrefs,
            mockDailyReminderManager,
        )

        verify { mockDailyReminderManager.scheduleReminder() }
    }

    @Test
    fun `should not schedule reminder when user is signed in and reminder is disabled`() {
        every { mockFirebaseAuth.currentUser }.returns(mockk())
        every { mockSSPrefs.reminderEnabled() }.returns(false)

        SplashViewModel(
            mockFirebaseAuth,
            mockSSPrefs,
            mockDailyReminderManager,
        )

        verify(inverse = false) { mockDailyReminderManager.scheduleReminder() }
    }

    @Test
    fun `should return Quarterlies state when user is signed in and no last saved index`() {
        every { mockFirebaseAuth.currentUser }.returns(mockk())
        every { mockSSPrefs.getLastQuarterlyIndex() }.returns(null)

        val state = viewModel.launchState
        state shouldBeEqualTo LaunchState.Quarterlies
    }

    @Test
    fun `should return Quarterlies state when user is signed in with last saved index and not reading latest quarterly`() {
        every { mockFirebaseAuth.currentUser }.returns(mockk())
        every { mockSSPrefs.getLastQuarterlyIndex() }.returns("index")
        every { mockSSPrefs.isReadingLatestQuarterly() }.returns(false)

        val state = viewModel.launchState
        state shouldBeEqualTo LaunchState.Quarterlies
    }

    @Test
    fun `should return Lessons state when user is signed in with last saved index and reading latest quarterly`() {
        every { mockFirebaseAuth.currentUser }.returns(mockk())
        every { mockSSPrefs.getLastQuarterlyIndex() }.returns("index")
        every { mockSSPrefs.isReadingLatestQuarterly() }.returns(true)

        val state = viewModel.launchState
        state shouldBeEqualTo LaunchState.Lessons("index")
    }
}
