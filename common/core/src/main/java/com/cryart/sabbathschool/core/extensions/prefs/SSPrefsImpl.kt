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
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.misc.SSHelper
import com.cryart.sabbathschool.core.model.ReminderTime
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ss_prefs",
    produceMigrations = {
        listOf(
            SharedPreferencesMigration(
                it,
                "${it.packageName}_preferences",
                setOf(
                    SSConstants.SS_SETTINGS_THEME_KEY,
                    SSConstants.SS_SETTINGS_SIZE_KEY,
                    SSConstants.SS_SETTINGS_FONT_KEY
                )
            )
        )
    }
)

class SSPrefsImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.dataStore,
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SSPrefs {

    private fun preferencesFlow(): Flow<Preferences> = dataStore.data
        .catch { exception ->
            Timber.e(exception)
            emit(emptyPreferences())
        }

    override fun reminderEnabled(): Boolean = sharedPreferences.getBoolean(
        SSConstants.SS_SETTINGS_REMINDER_ENABLED_KEY, true
    )

    override fun getReminderTime(): ReminderTime {
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

    override fun getReminderJobId(): Int? {
        val id = sharedPreferences.getInt(SSConstants.SS_REMINDER_JOB_ID, -1)
        return if (id == -1) {
            null
        } else {
            id
        }
    }

    override fun setReminderJobId(jobId: Int?) {
        sharedPreferences.edit {
            putInt(SSConstants.SS_REMINDER_JOB_ID, jobId ?: -1)
        }
    }

    override fun getLanguageCode(): String {
        val code = sharedPreferences.getString(
            SSConstants.SS_LAST_LANGUAGE_INDEX,
            Locale.getDefault().language
        )!!
        return mapLanguageCode(code)
    }

    override fun getLanguageCodeFlow(): Flow<String> = preferencesFlow()
        .map { preferences ->
            val code = preferences[stringPreferencesKey(SSConstants.SS_LAST_LANGUAGE_INDEX)] ?: getLanguageCode()
            mapLanguageCode(code)
        }
        .distinctUntilChanged()

    override fun setLanguageCode(languageCode: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_LANGUAGE_INDEX, languageCode)
        }

        coroutineScope.launch {
            dataStore.edit { settings ->
                settings[stringPreferencesKey(SSConstants.SS_LAST_LANGUAGE_INDEX)] = languageCode
            }
        }
    }

    private fun mapLanguageCode(code: String): String {
        return when (code) {
            "iw" -> "he"
            "fil" -> "tl"
            else -> code
        }
    }

    override fun getLastQuarterlyIndex(): String? {
        return sharedPreferences.getString(SSConstants.SS_LAST_QUARTERLY_INDEX, null)
    }

    override fun setLastQuarterlyIndex(index: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_QUARTERLY_INDEX, index)
        }
    }

    override fun isLanguagePromptSeen(): Boolean {
        return sharedPreferences.getBoolean(
            SSConstants.SS_LANGUAGE_FILTER_PROMPT_SEEN, false
        )
    }

    override fun setLanguagePromptSeen() {
        sharedPreferences.edit {
            putBoolean(SSConstants.SS_LANGUAGE_FILTER_PROMPT_SEEN, true)
        }
    }

    override fun getLastQuarterlyType(): String? {
        return sharedPreferences.getString(
            SSConstants.SS_LAST_QUARTERLY_TYPE,
            null
        )
    }

    override fun setLastQuarterlyType(type: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_QUARTERLY_TYPE, type)
        }
    }

    override fun getLastReaderArtifactCreationTime(): Long {
        return sharedPreferences.getLong(SSConstants.SS_READER_ARTIFACT_CREATION_TIME, 0)
    }

    override fun setLastReaderArtifactCreationTime(readerArtifactCreationTime: Long) {
        sharedPreferences.edit {
            putLong(SSConstants.SS_READER_ARTIFACT_CREATION_TIME, readerArtifactCreationTime)
        }
    }

    /**
     * By pre-loading the [SSReadingDisplayOptions] in [displayOptionsFlow]
     * Later synchronous reads may be faster or may avoid a disk I/O operation
     * altogether if the initial read has completed.
     */
    override fun getDisplayOptions(callback: (SSReadingDisplayOptions) -> Unit) {
        coroutineScope.launch {
            val settings = try {
                dataStore.data.first()
            } catch (ex: Exception) {
                Timber.e(ex)
                emptyPreferences()
            }

            callback(settings.toDisplayOptions())
        }
    }

    override fun displayOptionsFlow(): Flow<SSReadingDisplayOptions> = preferencesFlow()
        .map { preferences -> preferences.toDisplayOptions() }
        .distinctUntilChanged()

    private fun Preferences.toDisplayOptions(): SSReadingDisplayOptions {
        val defaultTheme = if (context.isDarkTheme()) {
            SSReadingDisplayOptions.SS_THEME_DARK
        } else {
            SSReadingDisplayOptions.SS_THEME_LIGHT
        }
        val theme = this[stringPreferencesKey(SSConstants.SS_SETTINGS_THEME_KEY)] ?: defaultTheme
        val size = this[stringPreferencesKey(SSConstants.SS_SETTINGS_SIZE_KEY)] ?: SSReadingDisplayOptions.SS_SIZE_MEDIUM
        val font = this[stringPreferencesKey(SSConstants.SS_SETTINGS_FONT_KEY)] ?: SSReadingDisplayOptions.SS_FONT_LATO

        return SSReadingDisplayOptions(theme, size, font)
    }

    override fun setDisplayOptions(ssReadingDisplayOptions: SSReadingDisplayOptions) {
        coroutineScope.launch {
            dataStore.edit { settings ->
                val themePrefKey = stringPreferencesKey(SSConstants.SS_SETTINGS_THEME_KEY)
                val fontPrefKey = stringPreferencesKey(SSConstants.SS_SETTINGS_FONT_KEY)
                val sizePrefKey = stringPreferencesKey(SSConstants.SS_SETTINGS_SIZE_KEY)

                if (settings[themePrefKey] != ssReadingDisplayOptions.theme) {
                    settings[themePrefKey] = ssReadingDisplayOptions.theme
                }
                if (settings[fontPrefKey] != ssReadingDisplayOptions.font) {
                    settings[fontPrefKey] = ssReadingDisplayOptions.font
                }
                if (settings[sizePrefKey] != ssReadingDisplayOptions.size) {
                    settings[sizePrefKey] = ssReadingDisplayOptions.size
                }
            }
        }
    }

    override fun getLastBibleUsed(): String? {
        return sharedPreferences.getString(SSConstants.SS_LAST_BIBLE_VERSION_USED, null)
    }

    override fun setLastBibleUsed(bibleId: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_BIBLE_VERSION_USED, bibleId)
        }
    }

    override fun isAppReBrandingPromptShown(): Boolean = sharedPreferences.getBoolean(
        SSConstants.SS_APP_RE_BRANDING_PROMPT_SEEN, false
    )

    override fun setAppReBrandingShown() = sharedPreferences.edit {
        putBoolean(SSConstants.SS_APP_RE_BRANDING_PROMPT_SEEN, true)
    }

    override fun clear() {
        sharedPreferences.edit { clear() }
        coroutineScope.launch {
            dataStore.edit { it.clear() }
        }
    }
}
