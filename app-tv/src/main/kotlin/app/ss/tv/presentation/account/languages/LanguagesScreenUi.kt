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

package app.ss.tv.presentation.account.languages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DenseListItem
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.ss.tv.data.model.LanguageSpec
import app.ss.tv.presentation.account.languages.LanguagesScreen.Event
import app.ss.tv.presentation.account.languages.LanguagesScreen.State
import app.ss.tv.presentation.theme.SSTvTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.android.components.ActivityComponent
import kotlinx.collections.immutable.persistentListOf
import app.ss.translations.R as L10nR

@CircuitInject(LanguagesScreen::class, ActivityComponent::class)
@Composable
fun LanguagesScreenUi(state: State, modifier: Modifier = Modifier) {
    val lazyListState = rememberLazyListState()
    var scrollToIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 72.dp),
        state = lazyListState
    ) {
        item {
            Text(
                text = stringResource(L10nR.string.ss_languages),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        when (state) {
            is State.Languages -> {
                languagesUi(state)
                scrollToIndex = state.languages.indexOfFirst { it.selected }
            }

            State.Loading -> Unit
            State.Error -> Unit
        }
    }

    LaunchedEffect(scrollToIndex) {
        lazyListState.animateScrollToItem(scrollToIndex, -200)
    }
}


private fun LazyListScope.languagesUi(state: State.Languages) {
    itemsIndexed(state.languages, key = { _, model -> model.code }) { _, model ->
        DenseListItem(
            selected = model.selected,
            onClick = { state.eventSink(Event.OnSelected(model.code)) },
            headlineContent = {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            modifier = Modifier.padding(top = 16.dp),
            trailingContent = {
                if (model.selected) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = stringResource(L10nR.string.ss_action_selected)
                    )
                }
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    SSTvTheme {
        LanguagesScreenUi(
            State.Languages(
                persistentListOf(
                    LanguageSpec("en", "English", false),
                    LanguageSpec("fr", "French", true),
                    LanguageSpec("es", "Spanish", false)
                )
            ) {},
            Modifier.padding(vertical = 16.dp)
        )
    }
}
