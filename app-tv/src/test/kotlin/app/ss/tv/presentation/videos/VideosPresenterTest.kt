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

import android.content.Intent
import app.ss.tv.data.infoModel
import app.ss.tv.data.model.CategorySpec
import app.ss.tv.data.repository.FakeVideosRepository
import app.ss.tv.data.videoSpec
import app.ss.tv.navigator.AndroidScreen
import app.ss.tv.presentation.FakeScrollEvents
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.prefs.api.test.FakeSSPrefs

class VideosPresenterTest {

    private val navigator = FakeNavigator()
    private val repository = FakeVideosRepository()
    private val languagesFlow = MutableStateFlow("en")
    private val fakeIntent = Intent()

    private val underTest = VideosPresenter(
        repository = repository,
        navigator = navigator,
        ssPrefs = FakeSSPrefs(languagesFlow),
        dispatcherProvider = TestDispatcherProvider(),
        scrollEvents = FakeScrollEvents(),
        intentHelper = { fakeIntent }
    )

    @Test
    fun `present - emit loading then error`() = runTest {
        underTest.test {
            awaitItem() shouldBeInstanceOf (VideosScreen.State.Loading::class)

            awaitItem() shouldBeInstanceOf (VideosScreen.State.Error::class)
        }
    }

    @Test
    fun `present - emit loading then videos`() = runTest {
        repository.videosResult = Result.success(listOf(infoModel))

        underTest.test {
            awaitItem() shouldBeInstanceOf (VideosScreen.State.Loading::class)

            (awaitItem() as VideosScreen.State.Videos).categories shouldBeEqualTo persistentListOf(
                CategorySpec(
                    id = "0",
                    title = infoModel.artist,
                    videos = persistentListOf(videoSpec)
                )
            )
        }
    }

    @Test
    fun `present - navigate to video player screen`() = runTest {
        repository.videosResult = Result.success(listOf(infoModel))

        underTest.test {
            awaitItem() shouldBeInstanceOf (VideosScreen.State.Loading::class)

            val state = awaitItem() as VideosScreen.State.Videos

            state.eventSink(VideosScreen.Event.OnVideoClick(videoSpec))

            navigator.awaitNextScreen() shouldBeEqualTo AndroidScreen.IntentScreen(fakeIntent)
        }
    }
}
