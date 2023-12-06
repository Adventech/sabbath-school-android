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

package app.ss.tv.presentation.player

import android.content.Context
import androidx.media3.ui.PlayerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.tv.data.ssVideo
import app.ss.tv.presentation.player.VideoPlayerScreen.Event
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import ss.libraries.media.test.FakeSSVideoPlayer

/** Unit tests for [VideoPlayerPresenter]. */
@RunWith(AndroidJUnit4::class)
class VideoPlayerPresenterTest {

    private val appContext: Context = ApplicationProvider.getApplicationContext()

    private val ambientModeHelper = FakeAmbientModeHelper()
    private val fakeVideoPlayer = FakeSSVideoPlayer()

    private val screen = VideoPlayerScreen(ssVideo)
    private val underTest = VideoPlayerPresenter(
        ambientModeHelper = ambientModeHelper,
        videoPlayer = fakeVideoPlayer,
        screen = screen,
    )

    @Test
    fun `present - should emit default state`() = runTest {
        underTest.test {
            val controls = awaitItem().controls

            controls.isPlaying shouldBeEqualTo false
            controls.isBuffering shouldBeEqualTo false
            controls.title shouldBeEqualTo ssVideo.title
            controls.artist shouldBeEqualTo ssVideo.artist

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - should play video when playerView is created`() = runTest {
        val playerView = PlayerView(appContext)
        underTest.test {
            val state = awaitItem()

            state.eventSink(Event.OnPlayerViewCreated(playerView))

            fakeVideoPlayer.video shouldBeEqualTo screen.video
            fakeVideoPlayer.playerView shouldBeEqualTo playerView

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - handle controls events`() = runTest {
        underTest.test {
            val controls = awaitItem().controls

            controls.onSeek(1000)
            fakeVideoPlayer.seekTo shouldBeEqualTo 1000

            controls.onPlayPauseToggle()
            fakeVideoPlayer.playPauseInvoked shouldBeEqualTo true

            ensureAllEventsConsumed()
        }
    }
}
