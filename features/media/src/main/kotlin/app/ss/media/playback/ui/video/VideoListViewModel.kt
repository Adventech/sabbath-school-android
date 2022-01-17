/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.media.playback.ui.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.model.api.SSVideosInfo
import app.ss.lessons.data.repository.media.SSMediaRepository
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.intent.lessonIndex
import com.cryart.sabbathschool.core.extensions.list.subList
import com.cryart.sabbathschool.core.response.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val repository: SSMediaRepository,
    private val schedulerProvider: SchedulerProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val videoListFlow: StateFlow<VideoListData> = flowOf(savedStateHandle.lessonIndex)
        .map { index ->
            index?.let {
                withContext(schedulerProvider.default) {
                    repository.getVideo(it)
                }
            } ?: Resource.success(emptyList())
        }.map { resource ->
            (resource.data ?: emptyList()).toData()
        }
        .stateIn(viewModelScope, VideoListData.Empty)

    private fun List<SSVideosInfo>.toData(): VideoListData {
        return if (size == 1 && first().clips.isNotEmpty()) {
            return VideoListData.Vertical(
                featured = first().clips.first(),
                clips = first().clips.subList(1)
            )
        } else {
            VideoListData.Horizontal(
                data = this,
                target = savedStateHandle.lessonIndex
            )
        }
    }
}
