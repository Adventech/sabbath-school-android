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

package ss.prefs.api.test

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ss.prefs.api.SSPrefs
import ss.prefs.model.ReminderTime
import ss.prefs.model.SSReadingDisplayOptions

/** Fake implementation of [SSPrefs] for use in tests. **/
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeSSPrefs(
    private val languagesFlow: MutableStateFlow<String> = MutableStateFlow("")
) : SSPrefs {

    var setLanguageCode: String? = null
        private set

    var reminderEnabledDelegate: () -> Boolean = { false }
    var reminderScheduledDelegate: () -> Boolean = { false }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun displayOptionsFlow(): Flow<SSReadingDisplayOptions> {
        TODO("Not yet implemented")
    }

    override fun setDisplayOptions(ssReadingDisplayOptions: SSReadingDisplayOptions) {
        TODO("Not yet implemented")
    }

    override fun getReminderTime(): ReminderTime {
        TODO("Not yet implemented")
    }

    override fun reminderTimeFlow(): Flow<ReminderTime> {
        TODO("Not yet implemented")
    }

    override fun setReminderTime(time: ReminderTime) {
        TODO("Not yet implemented")
    }

    override fun getLanguageCode(): String = languagesFlow.value

    override fun getLanguageCodeFlow(): Flow<String> = languagesFlow

    override fun setLanguageCode(languageCode: String) {
        setLanguageCode = languageCode
        languagesFlow.update { languageCode }
    }

    override fun reminderEnabled(): Boolean {
        return reminderEnabledDelegate()
    }

    override fun setReminderEnabled(enabled: Boolean) {

    }

    override fun isAppReBrandingPromptShown(): Boolean = true

    override fun isReminderScheduled(): Boolean {
        return reminderScheduledDelegate()
    }

    override fun setReaderArtifactLastModified(lastModified: String) {

    }

    override fun setAppReBrandingShown() {}

    override fun setReminderScheduled(scheduled: Boolean) {

    }
}
