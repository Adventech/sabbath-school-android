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

package com.cryart.sabbathschool.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.ss.auth.AuthRepository
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import ss.libraries.circuit.navigation.LessonsScreen
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.QuarterliesScreen
import ss.prefs.api.SSPrefs

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val ssPrefs: SSPrefs,
    private val authRepository: AuthRepository,
    private val dailyReminderManager: DailyReminderManager,
) : Presenter<HomeScreen.State> {

    @CircuitInject(HomeScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }

    @Composable
    override fun present(): HomeScreen.State {
        LaunchedEffect(key1 = Unit) {
            val user = authRepository.getUser().getOrNull()

            if (user != null && ssPrefs.reminderEnabled() && ssPrefs.isReminderScheduled().not()) {
                dailyReminderManager.scheduleReminder()
            }

            when {
                user == null -> navigator.resetRoot(LoginScreen)
                ssPrefs.getLastQuarterlyIndex() != null && ssPrefs.isReadingLatestQuarterly() -> {
                    navigator.resetRoot(QuarterliesScreen)
                    navigator.goTo(LessonsScreen(ssPrefs.getLastQuarterlyIndex()))
                }

                else -> navigator.resetRoot(QuarterliesScreen)
            }
        }

        return HomeScreen.State
    }
}
