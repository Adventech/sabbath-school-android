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

package app.ss.tv.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.ss.tv.presentation.Screens
import app.ss.tv.presentation.account.AccountScreen
import app.ss.tv.presentation.home.HomeScreen.Event
import app.ss.tv.presentation.home.HomeScreen.State
import app.ss.tv.presentation.videos.VideosScreen
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
) : Presenter<State> {

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }

    @Composable
    override fun present(): State {
        val selectedIndex by rememberRetained { mutableIntStateOf(0) }
        var currentScreen by rememberRetained { mutableStateOf<Screen>(VideosScreen) }

        return State(selectedIndex, currentScreen) { event ->
            when (event) {
                is Event.OnTopBarScreen -> {
                    currentScreen = when (event.screen) {
                        Screens.Account -> AccountScreen
                        Screens.Videos -> VideosScreen
                    }
                }

                Event.OnBack -> navigator.pop()
                is Event.OnNavEvent -> navigator.onNavEvent(event.event)
            }
        }
    }
}
