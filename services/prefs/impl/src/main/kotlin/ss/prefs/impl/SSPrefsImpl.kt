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

package ss.prefs.impl

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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.misc.DateHelper
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs
import ss.prefs.model.ReminderTime
import ss.prefs.model.SSReadingDisplayOptions
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
                    SSConstants.SS_SETTINGS_FONT_KEY,
                    SSConstants.SS_SETTINGS_REMINDER_TIME_KEY,
                )
            )
        )
    }
)


internal class SSPrefsImpl(
    private val dataStore: DataStore<Preferences>,
    private val sharedPreferences: SharedPreferences,
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : SSPrefs, Scopable by ioScopable(dispatcherProvider) {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        dispatcherProvider: DispatcherProvider,
    ) : this(
        dataStore = context.dataStore,
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        context = context,
        dispatcherProvider
    )

    private fun preferencesFlow(): Flow<Preferences> = dataStore.data
        .catch { exception ->
            Timber.e(exception)
            emit(emptyPreferences())
        }

    override fun reminderEnabled(): Boolean = sharedPreferences.getBoolean(
        SSConstants.SS_SETTINGS_REMINDER_ENABLED_KEY,
        true
    )

    override fun setReminderEnabled(enabled: Boolean) = sharedPreferences.edit {
        putBoolean(SSConstants.SS_SETTINGS_REMINDER_ENABLED_KEY, enabled)
    }

    override fun getReminderTime(): ReminderTime {
        val timeStr = sharedPreferences.getString(
            SSConstants.SS_SETTINGS_REMINDER_TIME_KEY,
            SSConstants.SS_SETTINGS_REMINDER_TIME_DEFAULT_VALUE
        )!!
        val hour = DateHelper.parseHourFromString(
            timeStr,
            SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT
        )
        val min = DateHelper.parseMinuteFromString(
            timeStr,
            SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT
        )

        return ReminderTime(hour, min)
    }

    override fun reminderTimeFlow(): Flow<ReminderTime> = preferencesFlow()
        .map { preferences ->
            val timeStr = preferences[stringPreferencesKey(SSConstants.SS_SETTINGS_REMINDER_TIME_KEY)] ?: SSConstants.SS_SETTINGS_REMINDER_TIME_DEFAULT_VALUE
            val hour = DateHelper.parseHourFromString(timeStr, SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT)
            val min = DateHelper.parseMinuteFromString(timeStr, SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT)
            ReminderTime(hour, min)
        }

    override fun setReminderTime(time: ReminderTime) {
        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", time.hour, time.min)
        sharedPreferences.edit {
            putString(SSConstants.SS_SETTINGS_REMINDER_TIME_KEY, formattedTime)
        }

        scope.launch {
            dataStore.edit { settings ->
                settings[stringPreferencesKey(SSConstants.SS_SETTINGS_REMINDER_TIME_KEY)] = formattedTime
            }
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
        .flowOn(dispatcherProvider.io)

    override fun setLanguageCode(languageCode: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_LAST_LANGUAGE_INDEX, languageCode)
        }

        scope.launch {
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

    override fun setReaderArtifactLastModified(lastModified: String) {
        sharedPreferences.edit {
            putString(SSConstants.SS_READER_ARTIFACT_LAST_MODIFIED, lastModified)
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
        scope.launch {
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

    override fun setReminderScheduled(scheduled: Boolean) = sharedPreferences.edit {
        putBoolean(SSConstants.SS_REMINDER_SCHEDULED, scheduled)
    }

    override fun clear() {
        sharedPreferences.edit { clear() }
        scope.launch {
            dataStore.edit { it.clear() }
        }
    }
}
