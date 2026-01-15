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

package ss.navigation.suite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import ss.libraries.circuit.navigation.FeedScreen
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.prefs.api.SSPrefs
import ss.resources.api.ResourcesRepository
import ss.resources.model.LanguageModel
import ss.services.circuit.impl.navigator.AndroidSupportingNavigator
import timber.log.Timber

class HomeNavigationPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val resourcesRepository: ResourcesRepository,
    private val ssPrefs: SSPrefs,
    private val navigatorFactory: AndroidSupportingNavigator.Factory
) : Presenter<State> {

    @CircuitInject(HomeNavScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomeNavigationPresenter
    }

    @Composable
    override fun present(): State {
        val navigationItems by rememberNavbarItems()
        val selectedScreen by rememberNavScreen()

        val items = navigationItems
        return when {
            items == null -> State.Loading
            items.isNotEmpty() -> State.NavbarNavigation(
                selectedItem = selectedScreen,
                items = items,
                navigatorFactory = navigatorFactory,
                eventSink = { event ->
                    when (event) {
                        is State.NavbarNavigation.Event.OnItemSelected -> {
                            val screen = event.item.screen()
                            ssPrefs.setLastNavigationScreen(screen.type.name)
                        }

                        is State.NavbarNavigation.Event.OnNavEvent -> {
                            navigator.onNavEvent(event.navEvent)
                        }
                    }
                }
            )
            else -> State.Fallback(
                selectedItem = FeedScreen(FeedScreen.Type.SABBATH_SCHOOL),
                eventSink = { event ->
                    when (event) {
                        is State.Fallback.Event.OnNavEvent -> {
                            navigator.onNavEvent(event.navEvent)
                        }
                    }
                }
            )
        }
    }

    @Composable
    private fun rememberNavScreen(): androidx.compose.runtime.State<FeedScreen> {
        val defaultScreen = FeedScreen(FeedScreen.Type.SABBATH_SCHOOL)

        return produceRetainedState(initialValue = defaultScreen) {
            ssPrefs.lastNavigationScreen()
                .map { name ->
                    val type = name?.let(FeedScreen.Type::fromName)
                        ?: FeedScreen.Type.SABBATH_SCHOOL
                    FeedScreen(type)
                }
                .catch {
                    Timber.e(it)
                    emit(defaultScreen)
                }
                .collect { value = it }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    private fun rememberNavbarItems() = produceRetainedState<ImmutableList<NavbarItem>?>(initialValue = null) {
        ssPrefs.getLanguageCodeFlow()
            .flatMapConcat { getLanguageNavigation(it) }
            .collect { value = it }
    }

    /** Returns a list of [NavbarItem]s based on the selected [language]. */
    private fun getLanguageNavigation(language: String): Flow<ImmutableList<NavbarItem>> {
        return resourcesRepository.language(language)
            .map { it.toNavbarItems() }
            .catch {
                Timber.e(it, "Failed to get Navbar items for language: $language")
                emit(persistentListOf())
            }
    }

    private fun LanguageModel.toNavbarItems(): ImmutableList<NavbarItem> {
        if (!aij && !pm && !devo && !explore) {
            return persistentListOf()
        }
        return buildList {
            add(NavbarItem.SabbathSchool)
            if (aij) add(NavbarItem.AliveInJesus)
            if (pm) add(NavbarItem.PersonalMinistries)
            if (devo) add(NavbarItem.Devotionals)
            if (explore) add(NavbarItem.Explore)
        }.toImmutableList()
    }
}
