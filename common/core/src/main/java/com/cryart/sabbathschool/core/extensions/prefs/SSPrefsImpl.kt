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

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.cryart.sabbathschool.core.extensions.context.systemService
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.misc.SSHelper
import com.cryart.sabbathschool.core.model.ReminderTime
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import dagger.hilt.android.qualifiers.ApplicationContext
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
import javax.inject.Inject

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

internal class SSPrefsImpl(
    private val dataStore: DataStore<Preferences>,
    private val sharedPreferences: SharedPreferences,
    private val coroutineScope: CoroutineScope,
    private val context: Context
) : SSPrefs {

    @Inject
    constructor(@ApplicationContext context: Context) : this(
        dataStore = context.dataStore,
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        coroutineScope = CoroutineScope(Dispatchers.IO),
        context = context
    )

    init {
        handleAppLocalesPref()
    }

    private fun preferencesFlow(): Flow<Preferences> = dataStore.data
        .catch { exception ->
            Timber.e(exception)
            emit(emptyPreferences())
        }

    override fun reminderEnabled(): Boolean = sharedPreferences.getBoolean(
        SSConstants.SS_SETTINGS_REMINDER_ENABLED_KEY,
        true
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

    override fun setLastQuarterlyIndex(index: String?) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_QUARTERLY_INDEX, index)
        }
    }

    override fun getReaderArtifactLastModified(): String? {
        return sharedPreferences.getString(SSConstants.SS_READER_ARTIFACT_LAST_MODIFIED, null)
    }

    override fun setReaderArtifactLastModified(lastModified: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_READER_ARTIFACT_LAST_MODIFIED, lastModified)
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
        val theme = this[stringPreferencesKey(SSConstants.SS_SETTINGS_THEME_KEY)] ?: SSReadingDisplayOptions.SS_THEME_DEFAULT
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

    override fun isAppReBrandingPromptShown(): Boolean = sharedPreferences.getBoolean(
        SSConstants.SS_APP_RE_BRANDING_PROMPT_SEEN,
        false
    )

    override fun isReminderScheduled(): Boolean = sharedPreferences.getBoolean(
        SSConstants.SS_REMINDER_SCHEDULED,
        false
    )

    override fun setAppReBrandingShown() = sharedPreferences.edit {
        putBoolean(SSConstants.SS_APP_RE_BRANDING_PROMPT_SEEN, true)
    }

    override fun setThemeColor(primary: String, primaryDark: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_COLOR_THEME_LAST_PRIMARY, primary)
            putString(SSConstants.SS_COLOR_THEME_LAST_PRIMARY_DARK, primaryDark)
        }
    }

    override fun setReminderScheduled() = sharedPreferences.edit {
        putBoolean(SSConstants.SS_REMINDER_SCHEDULED, true)
    }

    override fun isReadingLatestQuarterly(): Boolean {
        return sharedPreferences.getBoolean(SSConstants.SS_LATEST_QUARTERLY, false)
    }

    override fun setReadingLatestQuarterly(state: Boolean) = sharedPreferences.edit {
        putBoolean(SSConstants.SS_LATEST_QUARTERLY, state)
    }

    override fun clear() {
        sharedPreferences.edit { clear() }
        coroutineScope.launch {
            dataStore.edit { it.clear() }
        }
    }

    private fun handleAppLocalesPref() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locale = context.systemService<LocaleManager>(Context.LOCALE_SERVICE)
                .applicationLocales
                .takeUnless { it.isEmpty }?.get(0) ?: Locale.forLanguageTag(getLanguageCode())

            setLanguageCode(locale.language)
        }
    }
}
