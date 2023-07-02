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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.settings.repository

import app.ss.auth.AuthRepository
import app.ss.lessons.data.repository.user.UserDataRepository
import app.ss.models.config.AppConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.prefs.api.SSPrefs
import ss.prefs.model.ReminderTime
import ss.settings.DailyReminder

class SettingsRepositoryTest {

    private val mockAppConfig: AppConfig = mockk()
    private val mockPrefs: SSPrefs = mockk()
    private val mockDailyReminder: DailyReminder = mockk()
    private val mockAuthRepository: AuthRepository = mockk()
    private val mockUserRepository: UserDataRepository = mockk()

    private lateinit var repository: SettingsRepository

    @Before
    fun setup() {
        every { mockAppConfig.version }.returns("1.0.0")
        every { mockPrefs.reminderEnabled() }.returns(false)
        every { mockPrefs.getReminderTime() }.returns(ReminderTime(8, 0))

        repository = SettingsRepositoryImpl(
            context = mockk(),
            appConfig = mockAppConfig,
            prefs = mockPrefs,
            dailyReminder = mockDailyReminder,
            authRepository = mockAuthRepository,
            dispatcherProvider = TestDispatcherProvider(),
            userDataRepository = mockUserRepository
        )
    }

    @Test
    fun `should return entities`() {
        val entities = repository.buildEntities {}

        entities.size shouldBeEqualTo 14
    }

    @Test
    fun `setReminderTime - should update prefs and reschedule`() {
        every { mockPrefs.setReminderTime(ReminderTime(9, 30)) }.returns(Unit)
        every { mockDailyReminder.reSchedule() }.returns(Unit)

        repository.setReminderTime(9, 30)

        verify {
            mockPrefs.setReminderTime(ReminderTime(9, 30))
            mockDailyReminder.reSchedule()
        }
    }

    @Test
    fun `signOut - clear repositories`() = runTest {
        coEvery { mockUserRepository.clear() }.returns(Unit)
        coEvery { mockAuthRepository.logout() }.returns(Unit)

        repository.signOut()

        coVerify {
            mockUserRepository.clear()
            mockAuthRepository.logout()
        }
    }

    @Test
    fun `deleteAccount - clear repository and delete account`() = runTest {
        coEvery { mockUserRepository.clear() }.returns(Unit)
        coEvery { mockAuthRepository.deleteAccount() }.returns(Unit)

        repository.deleteAccount()

        coVerify {
            mockAuthRepository.deleteAccount()
            mockUserRepository.clear()
        }
    }
}
