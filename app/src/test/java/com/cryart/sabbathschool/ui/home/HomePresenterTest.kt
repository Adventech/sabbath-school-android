/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.ui.home

import android.content.Context
import app.ss.auth.test.FakeAuthRepository
import app.ss.models.auth.SSUser
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import ss.libraries.appwidget.api.FakeAppWidgetHelper
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.LoginScreen
import ss.prefs.api.test.FakeSSPrefs

class HomePresenterTest {

    private val fakeNavigator = FakeNavigator(HomeScreen)
    private val fakeAuthRepository = FakeAuthRepository()
    private val fakePrefs = FakeSSPrefs()
    private val fakeDailyReminderManager = FakeDailyReminderManager()
    private val fakeAppWidgetHelper = FakeAppWidgetHelper()

    private val underTest = HomePresenter(
        navigator = fakeNavigator,
        ssPrefs = fakePrefs,
        authRepository = fakeAuthRepository,
        dailyReminderManager = fakeDailyReminderManager,
        appWidgetHelper = fakeAppWidgetHelper,
    )

    @Test
    fun `present - user authed - schedule reminder`() = runTest {
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        with(fakePrefs) {
            quarterlyIndexDelegate = { null }
            reminderEnabledDelegate = { true }
            reminderScheduledDelegate = { false }
        }

        underTest.test {
            awaitItem()

            fakeDailyReminderManager.reminderScheduled shouldBeEqualTo true
            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo HomeNavScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - user authed - reminder scheduled`() = runTest {
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        with(fakePrefs) {
            quarterlyIndexDelegate = { null }
            reminderEnabledDelegate = { true }
            reminderScheduledDelegate = { true }
        }

        underTest.test {
            awaitItem()

            fakeDailyReminderManager.reminderScheduled shouldBeEqualTo false
            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo HomeNavScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - no user - go to Login`() = runTest {
        fakeAuthRepository.userDelegate = { Result.success(null) }

        underTest.test {
            awaitItem()

            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo LoginScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - user authed - last index available - not reading latest quarterly - go to Lessons`() = runTest {
        val index = "index-1"
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        with(fakePrefs) {
            quarterlyIndexDelegate = { index }
            reminderEnabledDelegate = { false }
            isReadingLatestQuarterlyDelegate = { false }
        }

        underTest.test {
            awaitItem()

            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo HomeNavScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - refresh app widgets`() = runTest {
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakePrefs.reminderEnabledDelegate = { false }

        underTest.test {
            awaitItem()

            fakeAppWidgetHelper.isRefreshAllCalled shouldBeEqualTo true
            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo HomeNavScreen

            ensureAllEventsConsumed()
        }
    }
}

private class FakeDailyReminderManager : DailyReminderManager {
    var reminderScheduled: Boolean = false
        private set

    override fun scheduleReminder() {
        reminderScheduled = true
    }

    override fun reSchedule() {}
    override fun showNotification(context: Context) {}
    override fun cancel() {}
}
