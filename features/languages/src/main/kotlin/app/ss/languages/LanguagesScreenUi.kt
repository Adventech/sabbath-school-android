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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.divider.Divider
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.scaffold.SsScaffold
import app.ss.design.compose.widget.search.SearchInput
import app.ss.languages.list.LanguagesList
import app.ss.languages.state.Event
import app.ss.languages.state.LanguagesEvent
import app.ss.languages.state.State
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import ss.libraries.circuit.navigation.LanguagesScreen

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(LanguagesScreen::class, SingletonComponent::class)
@Composable
fun LanguagesScreenUi(state: State, modifier: Modifier) {
  SsScaffold(
      modifier = modifier,
      topBar = {
        SearchView(
            onQuery = { (state as? State.Languages)?.eventSink?.invoke(LanguagesEvent.Search(it)) },
            onNavBack = {
              when (state) {
                is State.Languages -> state.eventSink(LanguagesEvent.NavBack)
                is State.Loading -> state.eventSink(Event.NavBack)
              }
            },
            modifier =
                Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
                ),
        )
      },
  ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
      Divider()

      when (state) {
        is State.Loading -> Box(Modifier.weight(1f))
        is State.Languages ->
            LanguagesList(
                state.models,
                onItemClick = { state.eventSink(LanguagesEvent.Select(it)) },
                modifier = Modifier.weight(1f),
            )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    onQuery: (String) -> Unit,
    onNavBack: () -> Unit,
    modifier: Modifier = Modifier
) {
  var queryValue by rememberSaveable { mutableStateOf("") }

  SsTopAppBar(
      spec = TopAppBarSpec(TopAppBarType.Small),
      title = {
        SearchInput(
            value = queryValue,
            onQueryChange = {
              queryValue = it
              onQuery(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Search Languages",
        )
      },
      modifier = modifier,
      navigationIcon = { IconButton(onClick = onNavBack) { IconBox(icon = Icons.ArrowBack) } },
  )
}

@PreviewLightDark
@Composable
private fun SearchPreview() {
  SsTheme { Surface { SearchView(onQuery = {}, onNavBack = {}) } }
}
