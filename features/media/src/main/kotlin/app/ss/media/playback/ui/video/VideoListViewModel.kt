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

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.models.media.SSVideosInfo
import com.cryart.sabbathschool.core.extensions.intent.lessonIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import ss.foundation.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val repository: MediaRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val videoListFlow: StateFlow<VideoListData> = flowOf(savedStateHandle.lessonIndex)
        .mapNotNull { it }
        .flatMapLatest { index ->
            repository.getVideo(index)
        }.map { videos ->
            videos.toData(savedStateHandle.lessonIndex)
        }
        .stateIn(viewModelScope, VideoListData.Empty)
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
        target = lessonIndex
    )
}
