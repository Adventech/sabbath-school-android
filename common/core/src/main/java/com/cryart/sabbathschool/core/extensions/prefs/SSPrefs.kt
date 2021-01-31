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

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.misc.SSHelper
import com.cryart.sabbathschool.core.model.ReminderTime
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import java.util.Locale

class SSPrefs(context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getReminderTime(): ReminderTime {
        val timeStr = sharedPreferences.getString(
            SSConstants.SS_SETTINGS_REMINDER_TIME_KEY,
            SSConstants.SS_SETTINGS_REMINDER_TIME_DEFAULT_VALUE
        )
        val hour = SSHelper.parseHourFromString(
            timeStr,
            SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT
        )
        val min = SSHelper.parseMinuteFromString(
            timeStr,
            SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT
        )

        return ReminderTime(hour, min)
    }

    fun getReminderJobId(): Int? {
        val id = sharedPreferences.getInt(SSConstants.SS_REMINDER_JOB_ID, -1)
        return if (id == -1) {
            null
        } else {
            id
        }
    }

    fun setReminderJobId(id: Int?) {
        sharedPreferences.edit {
            putInt(SSConstants.SS_REMINDER_JOB_ID, id ?: -1)
        }
    }

    fun getLanguageCode(): String {
        return sharedPreferences.getString(
            SSConstants.SS_LAST_LANGUAGE_INDEX,
            Locale.getDefault().language
        )!!
    }

    fun setLanguageCode(languageCode: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_LANGUAGE_INDEX, languageCode)
        }
    }

    fun getLastQuarterlyIndex(): String? {
        return sharedPreferences.getString(SSConstants.SS_LAST_QUARTERLY_INDEX, null)
    }

    fun setLastQuarterlyIndex(index: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_QUARTERLY_INDEX, index)
        }
    }

    fun isLanguagePromptSeen(): Boolean {
        return sharedPreferences.getBoolean(
            SSConstants.SS_LANGUAGE_FILTER_PROMPT_SEEN, false
        )
    }

    fun setLanguagePromptSeen() {
        sharedPreferences.edit {
            putBoolean(SSConstants.SS_LANGUAGE_FILTER_PROMPT_SEEN, true)
        }
    }

    fun getLastQuarterlyType(): String? {
        return sharedPreferences.getString(
            SSConstants.SS_LAST_QUARTERLY_TYPE,
            null
        )
    }

    fun setLastQuarterlyType(type: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_QUARTERLY_TYPE, type)
        }
    }

    fun getLastReaderArtifactCreationTime(): Long {
        return sharedPreferences.getLong(SSConstants.SS_READER_ARTIFACT_CREATION_TIME, 0)
    }

    fun setLastReaderArtifactCreationTime(readerArtifactCreationTime: Long) {
        sharedPreferences.edit {
            putLong(SSConstants.SS_READER_ARTIFACT_CREATION_TIME, readerArtifactCreationTime)
        }
    }

    fun getDisplayOptions(): SSReadingDisplayOptions {
        return SSReadingDisplayOptions(
            sharedPreferences.getString(SSConstants.SS_SETTINGS_THEME_KEY, SSReadingDisplayOptions.SS_THEME_LIGHT),
            sharedPreferences.getString(SSConstants.SS_SETTINGS_SIZE_KEY, SSReadingDisplayOptions.SS_SIZE_MEDIUM),
            sharedPreferences.getString(SSConstants.SS_SETTINGS_FONT_KEY, SSReadingDisplayOptions.SS_FONT_LATO)
        )
    }

    fun setDisplayOptions(ssReadingDisplayOptions: SSReadingDisplayOptions) {
        sharedPreferences.edit {
            putString(SSConstants.SS_SETTINGS_THEME_KEY, ssReadingDisplayOptions.theme)
            putString(SSConstants.SS_SETTINGS_FONT_KEY, ssReadingDisplayOptions.font)
            putString(SSConstants.SS_SETTINGS_SIZE_KEY, ssReadingDisplayOptions.size)
        }
    }

    fun getLastBibleUsed(): String? {
        return sharedPreferences.getString(SSConstants.SS_LAST_BIBLE_VERSION_USED, null)
    }

    fun setLastBibleUsed(bibleId: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_BIBLE_VERSION_USED, bibleId)
        }
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }
}
