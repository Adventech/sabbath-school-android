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

package app.ss.languages

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import app.ss.languages.state.LanguageModel
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.Language
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ss.foundation.coroutines.DispatcherProvider
import ss.prefs.api.SSPrefs
import ss.workers.api.WorkScheduler

internal class LanguagesPresenter
@AssistedInject
constructor(
    @Assisted private val navigator: Navigator,
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val workScheduler: WorkScheduler,
    private val dispatcherProvider: DispatcherProvider,
) : Presenter<State> {

  @AssistedFactory
  interface Factory {
    fun create(navigator: Navigator): LanguagesPresenter
  }

  @Composable
  override fun present(): State {
    var query by rememberRetained { mutableStateOf<String?>(null) }
    val viewModels by
        produceRetainedState<ImmutableList<LanguageModel>?>(
            initialValue = null,
            key1 = query,
        ) {
          repository
              .getLanguages(query)
              .map { it.getOrElse { emptyList() } }
              .flowOn(dispatcherProvider.default)
              .collect { languages -> value = languages.toModels() }
        }

    return when (val models = viewModels) {
      null ->
          State.Loading { event ->
            when (event) {
              Event.NavBack -> navigator.pop()
            }
          }
      else ->
          State.Languages(models) { event ->
            when (event) {
              is LanguagesEvent.Select -> {
                modelSelected(event.model)
                navigator.pop()
              }
              LanguagesEvent.NavBack -> navigator.pop()
              is LanguagesEvent.Search -> query = event.query?.trim()
            }
          }
    }
  }

  private fun modelSelected(model: LanguageModel) {
    ssPrefs.setLanguageCode(model.code)
    ssPrefs.setLastQuarterlyIndex(null)

    val appLocale = LocaleListCompat.forLanguageTags(model.code)
    AppCompatDelegate.setApplicationLocales(appLocale)

    workScheduler.preFetchImages(model.code)
  }

  private fun List<Language>.toModels() =
      map {
            LanguageModel(
                code = it.code,
                nativeName = it.nativeName,
                name = it.name,
                selected = it.code == ssPrefs.getLanguageCode(),
            )
          }
          .toImmutableList()
}
