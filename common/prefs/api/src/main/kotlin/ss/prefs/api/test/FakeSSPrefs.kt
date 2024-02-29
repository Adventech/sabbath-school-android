/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.annotations.VisibleForTesting
import ss.prefs.api.SSPrefs
import ss.prefs.api.ThemeColor
import ss.prefs.model.ReminderTime
import ss.prefs.model.SSReadingDisplayOptions

/** Fake implementation of [SSPrefs] for use in tests. **/
@VisibleForTesting
class FakeSSPrefs(
    private val languagesFlow: MutableStateFlow<String> = MutableStateFlow("")
) : SSPrefs {

    var setLanguageCode: String? = null
        private set
    var setLastQuarterlyIndex: String? = ""
        private set

    var quarterlyIndexDelegate: () -> String? = { throw NotImplementedError() }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun getDisplayOptions(callback: (SSReadingDisplayOptions) -> Unit) {
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

    override fun getLastQuarterlyIndex(): String? = quarterlyIndexDelegate()

    override fun getReaderArtifactLastModified(): String? {
        TODO("Not yet implemented")
    }

    override fun reminderEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setReminderEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isAppReBrandingPromptShown(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isReminderScheduled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setLastQuarterlyIndex(index: String?) {
        setLastQuarterlyIndex = index
    }

    override fun setReaderArtifactLastModified(lastModified: String) {
        TODO("Not yet implemented")
    }

    override fun setAppReBrandingShown() {
        TODO("Not yet implemented")
    }

    override fun setThemeColor(primary: String, primaryDark: String) {
        TODO("Not yet implemented")
    }

    override fun getThemeColor(): ThemeColor {
        TODO("Not yet implemented")
    }

    override fun setReminderScheduled(scheduled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isReadingLatestQuarterly(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setReadingLatestQuarterly(state: Boolean) {
        TODO("Not yet implemented")
    }
}
