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

package app.ss.media.playback.ui.nowPlaying

import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.ss.design.compose.theme.SsTheme
import app.ss.media.playback.model.PlaybackProgressState
import app.ss.media.playback.model.PlaybackQueue
import app.ss.media.playback.model.PlaybackSpeed
import app.ss.media.playback.ui.spec.PlaybackStateSpec
import app.ss.models.media.AudioFile
import io.mockk.mockk
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

@Ignore("Target sdk 33")
class NowPlayingScreenTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5
    )

    private lateinit var spec: NowPlayingScreenSpec

    @Before
    fun setup() {
        spec = NowPlayingScreenSpec(
            nowPlayingAudio = AudioFile(
                "",
                title = "Sabbath Rest",
                artist = "Adult Bible Study Guides",
                imageRatio = "portrait"
            ),
            playbackQueue = PlaybackQueue(),
            playbackState = PlaybackStateSpec(
                isPlaying = true,
                isPlayEnabled = true,
                isError = false,
                isBuffering = false,
                canShowMini = true
            ),
            playbackProgressState = PlaybackProgressState(
                total = TimeUnit.MINUTES.toMillis(3),
                elapsed = TimeUnit.MINUTES.toMillis(1),
                position = TimeUnit.MINUTES.toMillis(1)
            ),
            playbackConnection = mockk(),
            playbackSpeed = PlaybackSpeed.NORMAL,
            isDraggable = { }
        )
    }

    @Test
    fun now_playing_screen() = launch(spec)

    @Test
    fun now_playing_screen_dark() = launch(spec, true)

    private fun launch(
        spec: NowPlayingScreenSpec,
        darkTheme: Boolean = false
    ) {
        paparazzi.snapshot {
            SsTheme(darkTheme = darkTheme) {
                Surface {
                    NowPlayingScreen(spec = spec)
                }
            }
        }
    }
}
