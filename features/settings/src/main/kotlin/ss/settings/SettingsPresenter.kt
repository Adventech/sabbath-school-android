/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package ss.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.ss.design.compose.extensions.list.ListEntity
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ss.circuit.helpers.navigator.AndroidScreen
import ss.settings.SettingsScreen.Event
import ss.settings.SettingsScreen.State
import ss.settings.repository.SettingsRepository

internal class SettingsPresenter @AssistedInject constructor(
    private val repository: SettingsRepository,
    @Assisted private val navigator: Navigator,
) : Presenter<State> {

    @Composable
    override fun present(): State {
       var isSwitchChecked: Boolean? by rememberSaveable { mutableStateOf(null) }

        val entities by produceState<List<ListEntity>>(emptyList(), isSwitchChecked) {
            value = repository.buildEntities(
                onCheckedChange = { checked ->
                    isSwitchChecked = checked
                },
                onGoToUrl = { navigator.goTo(AndroidScreen.CustomTabsIntentScreen(it)) }
            )
        }
        return State(entities) { event ->
            when (event) {
                Event.NavBack -> navigator.pop()
            }
        }
    }

    @AssistedFactory
    internal interface Factory {
        fun create(
            navigator: Navigator,
        ): SettingsPresenter
    }
}
