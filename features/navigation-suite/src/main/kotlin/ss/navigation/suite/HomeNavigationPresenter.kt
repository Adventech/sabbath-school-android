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

package ss.navigation.suite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.QuarterliesScreen
import ss.prefs.api.SSPrefs
import ss.resources.api.ResourcesRepository

class HomeNavigationPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val resourcesRepository: ResourcesRepository,
    private val ssPrefs: SSPrefs,
) : Presenter<State> {

    @CircuitInject(HomeNavScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomeNavigationPresenter
    }

    @Composable
    override fun present(): State {
        val navigationItems by rememberNavbarItems()
        var hasNavigation by rememberRetained(navigationItems) { mutableStateOf(navigationItems?.isNotEmpty() == true) }
        var selectedScreen by rememberRetained(hasNavigation) { mutableStateOf<Screen>(QuarterliesScreen(hasNavigation)) }

        val items = navigationItems
        return when {
            items == null -> State.Loading
            items.isEmpty() -> State.Fallback(
                selectedItem = QuarterliesScreen(false),
                eventSink = { event ->
                    when (event) {
                        is State.Fallback.Event.OnNavEvent -> {
                            navigator.onNavEvent(event.navEvent)
                        }
                    }
                }
            )
            else -> State.NavbarNavigation(
                selectedItem = selectedScreen,
                items = items,
                eventSink = { event ->
                    when (event) {
                        is State.NavbarNavigation.Event.OnItemSelected -> {
                            selectedScreen = event.item.screen()
                        }

                        is State.NavbarNavigation.Event.OnNavEvent -> {
                            navigator.onNavEvent(event.navEvent)
                        }
                    }
                }
            )
        }
    }

    @Composable
    private fun rememberNavbarItems() = produceRetainedState<ImmutableList<NavbarItem>?>(initialValue = null) {
        ssPrefs.getLanguageCodeFlow()
            .map { fetchItems(it) }
            .collect { value = it }
    }

    /** Returns a list of [NavbarItem]s based on the selected [language]. */
    private suspend fun fetchItems(language: String): ImmutableList<NavbarItem> {
        val languages = resourcesRepository.languages()
        val model = languages.getOrElse { emptyList() }
            .firstOrNull { it.code == language }
            ?.takeUnless { !it.aij && !it.pm && !it.devo }
            ?: return persistentListOf()

        return buildList {
            add(NavbarItem.SabbathSchool)
            if (model.aij) {
                add(NavbarItem.AliveInJesus)
            }
            if (model.pm) {
                add(NavbarItem.PersonalMinistries)
            }
            if (model.devo) {
                add(NavbarItem.Devotionals)
            }
            add(NavbarItem.Account)
        }.toImmutableList()
    }
}
