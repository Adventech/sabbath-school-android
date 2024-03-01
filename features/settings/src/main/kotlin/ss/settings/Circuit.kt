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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import app.ss.design.compose.extensions.list.ListEntity
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.collections.immutable.ImmutableList
import ss.libraries.circuit.navigation.SettingsScreen
import ss.settings.ui.SettingsScreenUi

internal sealed interface Event : CircuitUiEvent {
    data object NavBack : Event
    data object OverlayDismiss : Event
    data object AccountDeleteConfirmed : Event
    data class SetReminderTime(val hour: Int, val minute: Int) : Event
    data object RemoveDownloads : Event
}

@Immutable
internal data class State(
    val entities: ImmutableList<ListEntity>,
    val overlay: Overlay?,
    val eventSick: (Event) -> Unit,
) : CircuitUiState

@Stable
internal sealed interface Overlay {
    @Immutable
    data class SelectReminderTime(val hour: Int, val minute: Int) : Overlay
    data object ConfirmDeleteAccount : Overlay
    data object ConfirmRemoveDownloads : Overlay
}

internal class SettingsUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
    return when (screen) {
      is SettingsScreen -> ui<State> { state, modifier -> SettingsScreenUi(state, modifier) }
      else -> null
    }
  }
}

internal class SettingsPresenterFactory(
    private val presenter: SettingsPresenter.Factory,
) : Presenter.Factory {
  override fun create(
      screen: Screen,
      navigator: Navigator,
      context: CircuitContext,
  ): Presenter<*>? {
    return when (screen) {
      is SettingsScreen -> presenter.create(navigator)
      else -> null
    }
  }
}
