/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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
package app.ss.bible

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.models.SSBibleVerses
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.core.response.Result
import com.cryart.sabbathschool.core.response.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BibleVersesViewModel @Inject constructor(
    preferences: SSPrefs,
    private val lessonsRepository: LessonsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bibleVerses: StateFlow<Result<List<SSBibleVerses>>> = flowOf(
        savedStateHandle.get<String>(SSConstants.SS_READ_INDEX_EXTRA)
    ).map { readIndex ->
        readIndex?.let {
            val response = lessonsRepository.getDayRead(readIndex)
            val bibleVerses = response.data?.bible ?: emptyList()
            val version = lessonsRepository.getPreferredBibleVersion() ?: bibleVerses.firstOrNull()?.name ?: ""
            val verse = savedStateHandle.get<String>(SSConstants.SS_READ_VERSE_EXTRA)
            val content = bibleVerses.find { it.name == version }?.verses?.get(verse) ?: ""
            bibleContent.emit(BibleVersesState(version, content))

            bibleVerses
        } ?: emptyList()
    }
        .asResult()
        .stateIn(viewModelScope, Result.Loading)

    private val displayOptions: Flow<Result<SSReadingDisplayOptions>> = preferences.displayOptionsFlow()
        .asResult()

    private val bibleContent = MutableStateFlow(BibleVersesState())

    internal val uiState: StateFlow<BibleVersesScreenState> = combine(
        bibleVerses,
        displayOptions,
        bibleContent
    ) { bibleVersesResult, displayOptionsResult, content ->

        val bibleVerses = when (bibleVersesResult) {
            is Result.Success -> bibleVersesResult.data
            is Result.Error -> emptyList()
            else -> emptyList()
        }

        val displayOptions = when (displayOptionsResult) {
            is Result.Success -> displayOptionsResult.data
            is Result.Error -> null
            else -> null
        }

        BibleVersesScreenState(
            isLoading = false,
            toolbarState = ToolbarState.Success(
                displayOptions = displayOptions,
                bibleVersions = bibleVerses.map { it.name }.toSet(),
                preferredBibleVersion = content.version
            ),
            displayOptions = displayOptions,
            content = content.content
        )
    }
        .catch { Timber.e(it) }
        .stateIn(
            scope = viewModelScope,
            initialValue = BibleVersesScreenState()
        )

    fun versionSelected(version: String) {
        val verses = (bibleVerses.value as? Result.Success<List<SSBibleVerses>>)?.data
        val bibleVerses = verses?.find { it.name == version } ?: return
        val verse = savedStateHandle.get<String>(SSConstants.SS_READ_VERSE_EXTRA)

        viewModelScope.launch {
            lessonsRepository.savePreferredBibleVersion(bibleVerses.name)

            val content = bibleVerses.verses[verse] ?: ""
            bibleContent.emit(BibleVersesState(version, content))
        }
    }
}
