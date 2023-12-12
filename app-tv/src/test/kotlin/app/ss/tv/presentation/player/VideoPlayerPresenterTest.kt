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
import app.ss.tv.presentation.player.VideoPlayerScreen.State
import com.slack.circuit.test.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import ss.libraries.media.model.NowPlaying
import ss.libraries.media.model.SSMediaItem
import ss.libraries.media.model.extensions.NONE_PLAYING
import ss.libraries.media.model.extensions.toNowPlaying
import ss.libraries.media.test.FakeSSMediaPlayer

/** Unit tests for [VideoPlayerPresenter]. */
@RunWith(AndroidJUnit4::class)
class VideoPlayerPresenterTest {

    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val isConnected = MutableStateFlow(false)
    private val nowPlaying = MutableStateFlow(NONE_PLAYING.toNowPlaying())

    private val ambientModeHelper = FakeAmbientModeHelper()
    private val fakeMediaPlayer = FakeSSMediaPlayer(
        isConnected = isConnected,
        nowPlaying = nowPlaying,
    )

    private val screen = VideoPlayerScreen(ssVideo)
    private val underTest = VideoPlayerPresenter(
        ambientModeHelper = ambientModeHelper,
        mediaPlayer = fakeMediaPlayer,
        screen = screen,
    )

    @Test
    fun `present - should emit default state`() = runTest {
        underTest.test {
            val state = awaitItem()

            state shouldBeEqualTo State.Loading

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - should emit playing state`() = runTest {
        underTest.test {
            awaitItem()

            isConnected.update { true }

            val state = (awaitItem() as State.Playing)

            state.isPlaying shouldBeEqualTo false
            state.isBuffering shouldBeEqualTo false

            nowPlaying.update {
                NowPlaying(
                    id = "id",
                    title = ssVideo.title,
                    artist = ssVideo.artist,
                )
            }

            val controls = (awaitItem() as State.Playing).controls
            controls.title shouldBeEqualTo ssVideo.title
            controls.artist shouldBeEqualTo ssVideo.artist

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - should play video when playerView is created`() = runTest {
        val playerView = PlayerView(appContext)
        underTest.test {
            awaitItem()

            isConnected.update { true }

            val state = awaitItem() as State.Playing

            state.eventSink(Event.OnPlayerViewCreated(playerView))

            fakeMediaPlayer.mediaItems shouldBeEqualTo listOf(SSMediaItem.Video(screen.video))
            fakeMediaPlayer.playerView shouldBeEqualTo playerView

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - handle controls events`() = runTest {
        underTest.test {
            awaitItem()

            isConnected.update { true }

            val state = (awaitItem() as State.Playing)

            state.eventSink(Event.OnSeek(1000))
            fakeMediaPlayer.seekTo shouldBeEqualTo 1000

            state.eventSink(Event.OnPlayPause)
            fakeMediaPlayer.playPauseInvoked shouldBeEqualTo true

            ensureAllEventsConsumed()
        }
    }
}
