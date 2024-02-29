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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.ss.design.compose.extensions.list.ListEntity
import com.cryart.sabbathschool.core.navigation.Destination
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import ss.circuit.helpers.navigation.CustomTabsIntentScreen
import ss.circuit.helpers.navigation.LegacyDestination
import ss.settings.repository.SettingsEntity
import ss.settings.repository.SettingsRepository

internal class SettingsPresenter @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository,
    @Assisted private val navigator: Navigator,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        var isSwitchChecked by rememberRetained { mutableStateOf<Boolean?>(null) }
        var overlay by rememberRetained { mutableStateOf<Overlay?>(null) }

        val entities by produceRetainedState<List<ListEntity>>(emptyList(), isSwitchChecked, overlay) {
            repository.entitiesFlow { entity ->
                when (entity) {
                    SettingsEntity.Account.Delete -> {
                        overlay = Overlay.ConfirmDeleteAccount
                    }

                    SettingsEntity.Account.SignOut -> {
                        repository.signOut()
                        with(navigator) {
                            goTo(LegacyDestination(Destination.LOGIN))
                            pop()
                        }
                    }

                    is SettingsEntity.Reminder.Switch -> {
                        isSwitchChecked = entity.isChecked
                    }

                    is SettingsEntity.Reminder.Time -> {
                        overlay = Overlay.SelectReminderTime(entity.hour, entity.minute)
                    }

                    is SettingsEntity.About -> {
                        val event = CustomTabsIntentScreen(context.getString(entity.resId))
                        navigator.goTo(event)
                    }

                    SettingsEntity.Account.DeleteContent -> {
                        overlay = Overlay.ConfirmRemoveDownloads
                    }
                }
            }.collect { value = it }
        }
        return State(entities.toImmutableList(), overlay) { event ->
            when (event) {
                Event.NavBack -> navigator.pop()
                Event.OverlayDismiss -> {
                    overlay = null
                }

                Event.AccountDeleteConfirmed -> {
                    overlay = null
                    repository.deleteAccount()
                    with(navigator) {
                        goTo(LegacyDestination(Destination.LOGIN))
                        pop()
                    }
                }

                is Event.SetReminderTime -> {
                    repository.setReminderTime(event.hour, event.minute)
                    overlay = null
                }

                Event.RemoveDownloads -> {
                    repository.removeAllDownloads()
                    overlay = null
                }
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
