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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import app.ss.media.playback.ui.video.player.VideoPlayerActivity
import com.cryart.design.base.TransparentBottomSheetFragment
import com.cryart.design.base.TransparentBottomSheetSurface
import com.cryart.sabbathschool.core.misc.SSConstants
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoListFragment : TransparentBottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TransparentBottomSheetSurface {
                    ViewListScreen(
                        isAtTop = { isAtTop ->
                            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = isAtTop
                        },
                        onVideoClick = { video ->
                            val intent = VideoPlayerActivity.launchIntent(
                                requireActivity(),
                                video
                            )
                            requireActivity().startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

fun FragmentManager.showVideoList(
    lessonIndex: String,
) {
    val fragment = VideoListFragment().apply {
        arguments = bundleOf(
            SSConstants.SS_LESSON_INDEX_EXTRA to lessonIndex,
        )
    }
    fragment.show(this, "VideoList")
}
