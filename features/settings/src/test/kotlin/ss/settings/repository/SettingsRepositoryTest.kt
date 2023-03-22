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

import android.content.Context
import app.ss.models.config.AppConfig
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import ss.prefs.api.SSPrefs
import ss.prefs.model.ReminderTime
import ss.settings.DailyReminder

class SettingsRepositoryTest {

    private val mockContext: Context = mockk()
    private val mockAppConfig: AppConfig = mockk()
    private val mockPrefs: SSPrefs = mockk()
    private val mockDailyReminder: DailyReminder = mockk()

    private lateinit var repository: SettingsRepository

    @Before
    fun setup() {
        every { mockAppConfig.version }.returns("1.0.0")
        every { mockPrefs.reminderEnabled() }.returns(false)
        every { mockPrefs.getReminderTime() }.returns(ReminderTime(8, 0))

        repository = SettingsRepositoryImpl(
            context = mockContext,
            appConfig = mockAppConfig,
            prefs = mockPrefs,
            dailyReminder = mockDailyReminder
        )
    }

    @Test
    fun `should return entities`() {
        val entities = repository.buildEntities({}, {})

        entities.size shouldBeEqualTo 14
    }
}
