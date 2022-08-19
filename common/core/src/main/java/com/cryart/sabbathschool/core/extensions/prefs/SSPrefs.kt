/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.core.extensions.prefs

import com.cryart.sabbathschool.core.model.ReminderTime
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import kotlinx.coroutines.flow.Flow

interface SSPrefs {
    fun clear()
    fun getDisplayOptions(callback: (SSReadingDisplayOptions) -> Unit)
    fun displayOptionsFlow(): Flow<SSReadingDisplayOptions>
    fun setDisplayOptions(ssReadingDisplayOptions: SSReadingDisplayOptions)
    fun getReminderTime(): ReminderTime
    fun getLanguageCode(): String
    fun getLanguageCodeFlow(): Flow<String>
    fun setLanguageCode(languageCode: String)
    fun getLastQuarterlyType(): String?
    fun getLastQuarterlyIndex(): String?
    fun getReaderArtifactLastModified(): String?
    fun reminderEnabled(): Boolean
    fun setReminderEnabled(enabled: Boolean)
    fun isAppReBrandingPromptShown(): Boolean
    fun isReminderScheduled(): Boolean
    fun setLastQuarterlyType(type: String)
    fun setLastQuarterlyIndex(index: String)
    fun setReaderArtifactLastModified(lastModified: String)
    fun setAppReBrandingShown()
    fun setThemeColor(primary: String, primaryDark: String)
    fun setReminderScheduled(scheduled: Boolean = true)
    fun isReadingLatestQuarterly(): Boolean
    fun setReadingLatestQuarterly(state: Boolean)
}
