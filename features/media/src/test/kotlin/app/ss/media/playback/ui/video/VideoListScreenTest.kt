/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ss.design.compose.extensions.surface.ThemeSurface
import app.ss.models.media.SSVideosInfo
import com.cryart.sabbathschool.test.di.MockModule
import com.cryart.sabbathschool.test.di.mock.QuarterlyMockData
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VideoListScreenTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5
    )

    private lateinit var mockData: QuarterlyMockData

    @Before
    fun setup() {
        mockData = MockModule.provideQuarterlyMockData(
            context = paparazzi.context
        )
    }

    @Test
    fun video_list_grouped() = launch("en-2022-02-13", "videos.json")

    @Test
    fun video_list_single() = launch("en-2022-02-cq-13", "videos_cq.json")

    private fun launch(
        lessonIndex: String,
        fileName: String
    ) {
        val videos = mockData.getVideos(fileName).mapIndexed { index, model ->
            SSVideosInfo(
                id = "$lessonIndex-$index",
                artist = model.artist,
                clips = model.clips,
                lessonIndex = lessonIndex
            )
        }
        val data = videos.toData(lessonIndex)
        paparazzi.snapshot {
            ThemeSurface {
                VideoListScreen(videoList = data)
            }
        }
    }
}
