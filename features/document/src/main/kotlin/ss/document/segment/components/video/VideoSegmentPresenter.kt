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

package ss.document.segment.components.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.ss.models.media.SSVideo
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.Segment
import kotlinx.collections.immutable.toImmutableList
import ss.document.segment.components.video.VideoSegmentScreen.Event
import ss.document.segment.components.video.VideoSegmentScreen.State
import ss.libraries.media.api.MediaNavigation
import ss.resources.api.ResourcesRepository

class VideoSegmentPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: VideoSegmentScreen,
    private val resourcesRepository: ResourcesRepository,
    private val mediaNavigation: MediaNavigation,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val segment by rememberSegment()

        return State(
            title = segment?.title.orEmpty(),
            blocks = segment?.blocks.orEmpty(),
            videos = segment?.video.orEmpty().toImmutableList(),
        ) { event ->
            when (event) {
                is Event.OnNavBack -> navigator.pop()
                is Event.PlayVideo -> {
                    val video = event.video.run {
                        SSVideo(
                            artist = artist.orEmpty(),
                            id = "",
                            src = src,
                            target = "",
                            targetIndex = "",
                            thumbnail = thumbnail.orEmpty(),
                            title = title.orEmpty(),
                        )
                    }

                    navigator.goTo(IntentScreen(mediaNavigation.videoPlayer(event.context, video)))
                }
            }
        }
    }

    @Composable
    private fun rememberSegment() = produceRetainedState<Segment?>(null) {
        resourcesRepository.segment(screen.id).collect { value = it }
    }

    @CircuitInject(VideoSegmentScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: VideoSegmentScreen): VideoSegmentPresenter
    }
}
