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

package ss.feed

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ShareCompat
import app.ss.auth.AuthRepository
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
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.feed.FeedType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ss.feed.group.FeedGroupScreen
import ss.feed.model.FeedResourceSpec
import ss.feed.model.toSpec
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.FeedScreen
import ss.libraries.circuit.navigation.LanguagesScreen
import ss.libraries.circuit.navigation.LegacyDestination
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.ResourceScreen
import ss.libraries.circuit.navigation.SettingsScreen
import ss.misc.SSConstants
import ss.resources.api.ResourcesRepository
import ss.resources.model.FeedModel
import ss.services.auth.overlay.UserInfo
import app.ss.translations.R as L10nR
import ss.services.auth.overlay.AccountDialogOverlay.Result as OverlayResult

class FeedPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: FeedScreen,
    private val authRepository: AuthRepository,
    private val resourcesRepository: ResourcesRepository,
) : Presenter<State> {

    @CircuitInject(FeedScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: FeedScreen): FeedPresenter
    }

    @Composable
    override fun present(): State {
        val coroutineScope = rememberCoroutineScope()
        val feedType by rememberRetained { mutableStateOf(screen.type.toFeedType()) }
        val userInfo by produceRetainedState<UserInfo?>(initialValue = null) {
            value = authRepository.getUser().getOrNull()?.run {
                UserInfo(displayName, email, photo)
            }
        }
        val model by produceRetainedState<FeedModel?>(initialValue = null) {
            value = resourcesRepository.feed(screen.type.toFeedType()).getOrNull()
        }

        var overlayState = rememberRetained { mutableStateOf<OverlayState?>(null) }

        val eventSink: (Event) -> Unit = {
            eventSink(it, userInfo, overlayState, coroutineScope, feedType)
        }

        val feedModel = model
        return when {
            feedModel == null -> State.Loading(
                title = "",
                photoUrl = userInfo?.photo,
                overlayState = overlayState.value,
                eventSink = eventSink,
            )

            feedModel.groups.size == 1 -> State.List(
                photoUrl = userInfo?.photo,
                title = feedModel.title,
                resources = feedModel.groups.first().toSpec(),
                overlayState = overlayState.value,
                eventSink = eventSink,
            )

            else -> State.Group(
                photoUrl = userInfo?.photo,
                title = feedModel.title,
                groups = feedModel.groups.toImmutableList(),
                overlayState = overlayState.value,
                eventSink = eventSink,
            )
        }
    }

    private fun eventSink(
        event: Event,
        userInfo: UserInfo?,
        overlayState: MutableState<OverlayState?>,
        coroutineScope: CoroutineScope,
        feedType: FeedType,
    ) {
        when (event) {
            Event.FilterLanguages -> navigator.goTo(LanguagesScreen)
            Event.ProfileClick -> {
                userInfo?.let {
                    overlayState.value = OverlayState.AccountInfo(it) { result ->
                        overlayState.value = null
                        handleOverlayResult(result, coroutineScope)
                    }
                }
            }

            is SuccessEvent.OnItemClick -> {
                navigator.goTo(ResourceScreen(event.index))
            }

            is SuccessEvent.OnSeeAllClick -> {
                navigator.goTo(
                    FeedGroupScreen(
                        id = event.group.id,
                        title = event.group.title,
                        feedType = feedType
                    )
                )
            }
        }
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

            is OverlayResult.GoToPrivacyPolicy ->
                navigator.goTo(CustomTabsIntentScreen(result.context.getString(L10nR.string.ss_privacy_policy_url)))
        }
    }

    private fun FeedScreen.Type.toFeedType() = when (this) {
        FeedScreen.Type.ALIVE_IN_JESUS -> FeedType.AIJ
        FeedScreen.Type.PERSONAL_MINISTRIES -> FeedType.PM
        FeedScreen.Type.DEVOTIONALS -> FeedType.DEVO
        FeedScreen.Type.SABBATH_SCHOOL -> FeedType.SS
        FeedScreen.Type.EXPLORE -> FeedType.EXPLORE
    }

    private fun FeedGroup?.toSpec() = this?.resources?.map {
        it.toSpec(this, FeedResourceSpec.ContentDirection.HORIZONTAL)
    }?.toImmutableList() ?: persistentListOf()
}
