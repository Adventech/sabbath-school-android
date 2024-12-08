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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.ss.auth.AuthRepository
import app.ss.models.feed.FeedType
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.toImmutableList
import ss.libraries.circuit.navigation.FeedScreen
import ss.resources.api.ResourcesRepository
import ss.resources.model.FeedModel
import ss.services.auth.overlay.UserInfo

class FeedPresenter @AssistedInject constructor(
    @Assisted private val screen: FeedScreen,
    private val authRepository: AuthRepository,
    private val resourcesRepository: ResourcesRepository,
) : Presenter<State> {

    @CircuitInject(FeedScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(screen: FeedScreen): FeedPresenter
    }

    @Composable
    override fun present(): State {
        val userInfo by produceRetainedState<UserInfo?>(initialValue = null) {
            value = authRepository.getUser().getOrNull()?.run {
                UserInfo(displayName, email, photo)
            }
        }
        val model by produceRetainedState<FeedModel?>(initialValue = null) {
            value = resourcesRepository.feed(screen.type.toFeedType()).getOrNull()
        }

        val eventSink = { event: Event ->
            when (event) {
                Event.FilterLanguages -> {
                    // Navigate to LanguagesScreen
                }
                Event.ProfileClick -> {
                    // Navigate to AccountDialogOverlay
                }
                is SuccessEvent.OnItemClick -> {
                    // Navigate to FeedItemScreen
                }
                is SuccessEvent.OnSeeAllClick -> {
                    // Navigate to FeedGroupScreen
                }
            }
        }

        val feedModel = model
        return when {
            feedModel == null -> State.Loading(
                photoUrl = userInfo?.photo,
                eventSink = eventSink,
            )
            else -> State.Success(
                photoUrl = userInfo?.photo,
                title = feedModel.title,
                groups = feedModel.groups.toImmutableList(),
                eventSink = eventSink,
            )
        }
    }

    private fun FeedScreen.Type.toFeedType() = when (this) {
        FeedScreen.Type.ALIVE_IN_JESUS -> FeedType.AIJ
        FeedScreen.Type.PERSONAL_MINISTRIES -> FeedType.PM
        FeedScreen.Type.DEVOTIONALS -> FeedType.DEVO
    }
}
