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
import com.cryart.sabbathschool.BaseTest
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest : BaseTest() {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val mockFirebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val mockSchedulerProvider: SchedulerProvider = mockk()
    private val mockDailyReminderManager: DailyReminderManager = mockk()

    private lateinit var viewModel: SplashViewModel

    @Before
    fun setUp() {
        super.setup()

        every { mockSchedulerProvider.io }.returns(TestCoroutineDispatcher())
        every { mockDailyReminderManager.scheduleReminder() }.returns(Unit)

        viewModel = SplashViewModel(
            mockFirebaseAuth,
            mockDailyReminderManager,
            mockSchedulerProvider
        )
    }

    @Test
    fun `should emit false when user is not signed in`() {
        every { mockFirebaseAuth.currentUser }.returns(null)

        viewModel.isSignedInLiveData.observeForever {
            it.shouldBeFalse()
        }
    }

    @Test
    fun `should emit true when user signed in`() {
        every { mockFirebaseAuth.currentUser }.returns(mock())

        viewModel.isSignedInLiveData.observeForever {
            it.shouldBeTrue()
        }
    }

    @Test
    fun `should schedule reminder when user is signed in`() {
        every { mockFirebaseAuth.currentUser }.returns(mock())

        SplashViewModel(
            mockFirebaseAuth,
            mockDailyReminderManager,
            mockSchedulerProvider
        )

        verify { mockDailyReminderManager.scheduleReminder() }
    }
}
