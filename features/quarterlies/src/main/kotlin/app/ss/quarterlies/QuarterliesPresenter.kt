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

package app.ss.quarterlies

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.app.ShareCompat
import app.ss.auth.AuthRepository
import app.ss.quarterlies.list.QuarterliesListScreen
import app.ss.quarterlies.model.GroupedQuarterlies
import app.ss.quarterlies.model.placeHolderQuarterlies
import app.ss.quarterlies.overlay.UserInfo
import com.cryart.sabbathschool.core.navigation.Destination
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ss.lessons.api.repository.QuarterliesRepository
import ss.libraries.circuit.navigation.LanguagesScreen
import ss.libraries.circuit.navigation.LegacyDestination
import ss.libraries.circuit.navigation.LessonsScreen
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.QuarterliesScreen
import ss.libraries.circuit.navigation.SettingsScreen
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs
import timber.log.Timber
import app.ss.quarterlies.overlay.AccountDialogOverlay.Result as OverlayResult
import app.ss.translations.R as L10nR

class QuarterliesPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val repository: QuarterliesRepository,
    private val authRepository: AuthRepository,
    private val ssPrefs: SSPrefs,
    private val quarterliesUseCase: QuarterliesUseCase
) : Presenter<State> {

    @CircuitInject(QuarterliesScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): QuarterliesPresenter
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    override fun present(): State {
        val coroutineScope = rememberCoroutineScope()
        val userInfo by produceRetainedState<UserInfo?>(initialValue = null) {
            value = authRepository.getUser().getOrNull()?.run {
                UserInfo(displayName, email, photo)
            }
        }
        val quarterlies by produceRetainedState<GroupedQuarterlies>(initialValue = GroupedQuarterlies.TypeList(placeHolderQuarterlies())) {
            ssPrefs.getLanguageCodeFlow()
                .flatMapLatest { language -> repository.getQuarterlies(language) }
                .map(quarterliesUseCase::group)
                .catch { Timber.e(it) }
                .collect { value = it }
        }

        var overlayState by rememberRetained { mutableStateOf<OverlayState?>(null) }

        val showPrompt by rememberRetained(quarterlies) {
            mutableStateOf(!ssPrefs.isAppReBrandingPromptShown() && quarterlies.isNotEmpty())
        }
        LaunchedEffect(showPrompt) {
            if (showPrompt) {
                ssPrefs.setAppReBrandingShown()
                overlayState = OverlayState.BrandingInfo { overlayState = null }
            }
        }

        return State(
            photoUrl = userInfo?.photo,
            type = quarterlies,
            overlayState = overlayState,
            eventSink = { event ->
                when (event) {
                    is Event.QuarterlySelected -> navigator.goTo(LessonsScreen(event.index))

                    Event.FilterLanguages -> navigator.goTo(LanguagesScreen)
                    Event.ProfileClick -> {
                        userInfo?.let {
                            overlayState = OverlayState.AccountInfo(it) { result ->
                                overlayState = null
                                handleOverlayResult(result, coroutineScope)
                            }
                        }
                    }
                    is Event.SeeAll -> navigator.goTo(QuarterliesListScreen(event.group))
                }
            },
        )
    }

    private fun handleOverlayResult(result: OverlayResult, coroutineScope: CoroutineScope) {
        when (result) {
            OverlayResult.Dismiss -> Unit
            OverlayResult.GoToAbout -> navigator.goTo(LegacyDestination(Destination.ABOUT))
            OverlayResult.GoToSettings -> navigator.goTo(SettingsScreen)
            is OverlayResult.ShareApp -> {
                with(result.context) {
                    val shareIntent = ShareCompat.IntentBuilder(this)
                        .setType("text/plain")
                        .setText(getString(L10nR.string.ss_menu_share_app_text, SSConstants.SS_APP_PLAY_STORE_LINK))
                        .intent
                    if (shareIntent.resolveActivity(packageManager) != null) {
                        startActivity(Intent.createChooser(shareIntent, getString(L10nR.string.ss_menu_share_app)))
                    }
                }
            }

            OverlayResult.SignOut -> {
                coroutineScope.launch {
                    authRepository.logout()
                    navigator.resetRoot(LoginScreen)
                }
            }
        }
    }

    private fun GroupedQuarterlies.isNotEmpty(): Boolean {
        return when (this) {
            is GroupedQuarterlies.TypeList -> data.isNotEmpty() && data.any { it.isPlaceholder.not() }
            is GroupedQuarterlies.TypeGroup -> data.isNotEmpty()
            GroupedQuarterlies.Empty -> false
        }
    }
}
