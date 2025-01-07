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

package app.ss.media.playback.ui.video

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.media.playback.ui.video.player.VideoPlayerActivity
import app.ss.models.media.SSVideosInfo
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import ss.libraries.circuit.navigation.VideosScreen

class VideosScreenPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: VideosScreen,
    private val repository: MediaRepository,
) : Presenter<VideosScreenState> {

    @Composable
    override fun present(): VideosScreenState {
        val videos by rememberVideos()

        return VideosScreenState(data = videos) { event ->
            when (event) {
                is VideosScreenEvent.OnVideoSelected -> {
                    navigator.goTo(
                        IntentScreen(
                            VideoPlayerActivity.launchIntent(
                                event.context,
                                event.video,
                            )
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun rememberVideos() = produceRetainedState<VideoListData>(VideoListData.Empty) {
        repository.getVideo(screen.documentIndex)
            .map { it.toData(screen.documentIndex) }
            .collect { value = it }
    }

    @VisibleForTesting
    internal fun List<SSVideosInfo>.toData(
        lessonIndex: String?
    ): VideoListData = if (size == 1 && first().clips.isNotEmpty()) {
        val allVideos = first().clips
        val featuredVideo = allVideos.firstOrNull { it.targetIndex == lessonIndex } ?: allVideos.first()
        VideoListData.Vertical(
            featured = featuredVideo,
            clips = allVideos.subtract(setOf(featuredVideo)).toList(),
        )
    } else {
        VideoListData.Horizontal(
            data = this,
            target = lessonIndex,
        )
    }

    @CircuitInject(VideosScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: VideosScreen): VideosScreenPresenter
    }
}
