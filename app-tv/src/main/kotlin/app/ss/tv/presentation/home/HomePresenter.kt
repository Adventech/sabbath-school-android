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
import androidx.compose.runtime.produceState
import app.ss.tv.data.model.CategorySpec
import app.ss.tv.data.model.VideoSpec
import app.ss.tv.data.repository.VideosRepository
import app.ss.tv.presentation.home.HomeScreen.State
import app.ss.tv.presentation.player.VideoPlayerScreen
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ss.lessons.model.VideosInfoModel

class HomePresenter @AssistedInject constructor(
    private val repository: VideosRepository,
    @Assisted private val navigator: Navigator,
) : Presenter<State>  {

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }

    @Composable
    override fun present(): State {
        val videosInfo by produceState<List<VideosInfoModel>?>(emptyList()) {
            value = repository.getVideos().getOrNull()
        }

        return when {
            videosInfo == null -> State.Error
            videosInfo?.isEmpty() == true -> State.Loading
            else -> State.Videos(mapVideos(videosInfo!!)) { event ->
                when (event) {
                    is HomeScreen.Event.OnVideoClick -> navigator.goTo(VideoPlayerScreen(event.video))
                }
            }
        }
    }

    private fun mapVideos(
        videosInfo: List<VideosInfoModel>
    ) = videosInfo.mapIndexed { index, model ->
        CategorySpec(
            id = "$index",
            title = model.artist,
            videos = model.clips.map {
                VideoSpec(
                    id = it.id,
                    title = it.title,
                    artist = it.artist,
                    src = it.src,
                    thumbnail = it.thumbnail
                )
            }
        )
    }
}
