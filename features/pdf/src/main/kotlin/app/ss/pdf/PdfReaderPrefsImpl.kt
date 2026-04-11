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

package app.ss.pdf

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.configuration.theming.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.libraries.pdf.api.PdfReaderPrefs
import javax.inject.Inject
import javax.inject.Singleton

internal const val KEY_LAYOUT_MODE = "key:layout_mode"
internal const val KEY_SCROLL_MODE = "key:scroll_mode"
internal const val KEY_SCROLL_DIRECTION = "key:scroll_direction"
internal const val KEY_THEME_MODE = "key:theme_mode"

private val Context.pdfDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pdf_prefs",
    produceMigrations = {
        listOf(
            SharedPreferencesMigration(
                it,
                "${it.packageName}_preferences",
                setOf(
                    KEY_LAYOUT_MODE,
                    KEY_SCROLL_MODE,
                    KEY_SCROLL_DIRECTION,
                    KEY_THEME_MODE,
                )
            )
        )
    }
)

@Singleton
internal class PdfReaderPrefsImpl(
    private val dataStore: DataStore<Preferences>,
    private val sharedPreferences: SharedPreferences,
    dispatcherProvider: DispatcherProvider
) : PdfReaderPrefs, Scopable by ioScopable(dispatcherProvider) {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        dispatcherProvider: DispatcherProvider,
    ) : this(
        dataStore = context.pdfDataStore,
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        dispatcherProvider = dispatcherProvider
    )

    private val scrollModeKey = stringPreferencesKey(KEY_SCROLL_MODE)
    private val layoutModeKey = stringPreferencesKey(KEY_LAYOUT_MODE)
    private val scrollDirectionKey = stringPreferencesKey(KEY_SCROLL_DIRECTION)
    private val themeModeKey = stringPreferencesKey(KEY_THEME_MODE)

    override fun scrollMode(): Flow<PageScrollMode> = dataStore.data.map { preferences ->
        preferences[scrollModeKey]?.let {
            runCatching { PageScrollMode.valueOf(it) }.getOrNull()
        } ?: PageScrollMode.CONTINUOUS
    }

    override fun setScrollMode(mode: PageScrollMode) {
        sharedPreferences.edit { putString(KEY_SCROLL_MODE, mode.name) }
        scope.launch { dataStore.edit { it[scrollModeKey] = mode.name } }
    }

    override fun pageLayoutMode(): Flow<PageLayoutMode> = dataStore.data.map { preferences ->
        preferences[layoutModeKey]?.let {
            runCatching { PageLayoutMode.valueOf(it) }.getOrNull()
        } ?: PageLayoutMode.SINGLE
    }

    override fun setPageLayoutMode(mode: PageLayoutMode) {
        sharedPreferences.edit { putString(KEY_LAYOUT_MODE, mode.name) }
        scope.launch { dataStore.edit { it[layoutModeKey] = mode.name } }
    }

    override fun scrollDirection(): Flow<PageScrollDirection> = dataStore.data.map { preferences ->
        preferences[scrollDirectionKey]?.let {
            runCatching { PageScrollDirection.valueOf(it) }.getOrNull()
        } ?: PageScrollDirection.VERTICAL
    }

    override fun setScrollDirection(direction: PageScrollDirection) {
        sharedPreferences.edit { putString(KEY_SCROLL_DIRECTION, direction.name) }
        scope.launch { dataStore.edit { it[scrollDirectionKey] = direction.name } }
    }

    override fun themeMode(): Flow<ThemeMode> = dataStore.data.map { preferences ->
        preferences[themeModeKey]?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.DEFAULT
    }

    override fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit { putString(KEY_THEME_MODE, mode.name) }
        scope.launch { dataStore.edit { it[themeModeKey] = mode.name } }
    }

    override fun saveConfiguration(configuration: PdfConfiguration) {
        setScrollMode(configuration.scrollMode)
        setPageLayoutMode(configuration.layoutMode)
        setScrollDirection(configuration.scrollDirection)
        setThemeMode(configuration.themeMode)
    }
}
