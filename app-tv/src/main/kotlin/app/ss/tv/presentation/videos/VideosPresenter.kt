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

package app.ss.tv.presentation.videos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import app.ss.tv.data.model.CategorySpec
import app.ss.tv.data.model.VideoSpec
import app.ss.tv.data.repository.VideosRepository
import app.ss.tv.navigator.AndroidScreen
import app.ss.tv.navigator.IntentHelper
import app.ss.tv.presentation.ScrollEvents
import app.ss.tv.presentation.videos.VideosScreen.Event
import app.ss.tv.presentation.videos.VideosScreen.State
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.model.VideosInfoModel
import ss.prefs.api.SSPrefs
import timber.log.Timber

class VideosPresenter @AssistedInject constructor(
    private val repository: VideosRepository,
    private val ssPrefs: SSPrefs,
    private val dispatcherProvider: DispatcherProvider,
    private val scrollEvents: ScrollEvents,
    private val intentHelper: IntentHelper,
    @Assisted private val navigator: Navigator,
) : Presenter<State> {

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): VideosPresenter
    }

    @Composable
    override fun present(): State {
        val result by produceState(Result.success(emptyList<VideosInfoModel>())) {
            ssPrefs.getLanguageCodeFlow()
                .map { repository.getVideos(it) }
                .flowOn(dispatcherProvider.io)
                .catch { Timber.e(it) }
                .collect { value = it }
        }

        val eventSink: (Event) -> Unit = remember {
            { event ->
                when (event) {
                    is Event.OnVideoClick -> navigator.goTo(
                        AndroidScreen.IntentScreen(intentHelper.playerIntent(event.video))
                    )

                    Event.OnBack -> navigator.pop()
                    is Event.OnScroll -> scrollEvents.update(event.isTopAppBarVisible)
                }
            }
        }

        return when {
            result.isFailure -> State.Error(eventSink)
            result.getOrElse { emptyList() }.isEmpty() -> State.Loading(eventSink)
            else -> State.Videos(mapVideos(result.getOrThrow()), eventSink)
        }
    }

    private fun mapVideos(
        videosInfo: List<VideosInfoModel>
    ) = videosInfo.mapIndexed { index, model ->
        CategorySpec(
            id = "$index",
            title = model.artist,
            videos = model.clips.map { video ->
                VideoSpec(
                    id = video.id,
                    title = video.title,
                    artist = video.artist,
                    src = video.src,
                    thumbnail = video.thumbnail
                )
            }.toImmutableList()
        )
    }.toImmutableList()
}
