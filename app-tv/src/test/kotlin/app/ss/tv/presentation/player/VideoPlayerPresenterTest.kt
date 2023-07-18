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

import app.ss.tv.data.videoSpec
import app.ss.tv.presentation.player.VideoPlayerScreen.Event
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class VideoPlayerPresenterTest {

    private val navigator = FakeNavigator()
    private val ambientModeHelper = FakeAmbientModeHelper()

    private val underTest = VideoPlayerPresenter(
        ambientModeHelper = ambientModeHelper,
        screen = VideoPlayerScreen(videoSpec),
        navigator = navigator,
    )

    @Test
    fun `present - should emit state with video from screen`() = runTest {
        underTest.test {
            awaitItem().spec shouldBeEqualTo videoSpec
        }
    }

    @Test
    fun `present - navigator should pop after OnBack event`() = runTest {
        underTest.test {
            val state = awaitItem()

            state.eventSink(Event.OnBack)

            navigator.awaitPop()
        }
    }

    @Test
    fun `present - should update ambient mode on Playback events`() = runTest {
        underTest.test {
            val state = awaitItem()

            state.eventSink(Event.OnPlaybackChange(true))

            ambientModeHelper.enabled shouldBeEqualTo false

            state.eventSink(Event.OnPlaybackChange(false))

            ambientModeHelper.enabled shouldBeEqualTo true
        }
    }
}
