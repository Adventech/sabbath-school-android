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

package app.ss.tv.data.circuit

import app.ss.tv.presentation.dashboard.DashboardPresenter
import app.ss.tv.presentation.dashboard.DashboardScreen
import app.ss.tv.presentation.dashboard.DashboardScreenUi
import app.ss.tv.presentation.player.VideoPlayerPresenter
import app.ss.tv.presentation.player.VideoPlayerScreen
import app.ss.tv.presentation.player.VideoPlayerUiScreen
import app.ss.tv.presentation.videos.VideosPresenter
import app.ss.tv.presentation.videos.VideosScreen
import app.ss.tv.presentation.videos.VideosUiScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import javax.inject.Inject

interface SSPresenterFactory : Presenter.Factory
interface SSUiFactory : Ui.Factory

class SSPresenterFactoryImpl @Inject constructor(
    private val dashboardPresenter: DashboardPresenter.Factory,
    private val videosPresenter: VideosPresenter.Factory,
    private val videoPlayerPresenter: VideoPlayerPresenter.Factory,
) : SSPresenterFactory {

    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext
    ): Presenter<*>? {
        return when (screen) {
            is DashboardScreen -> dashboardPresenter.create(navigator)
            is VideosScreen -> videosPresenter.create(navigator)
            is VideoPlayerScreen -> videoPlayerPresenter.create(screen, navigator)
            else -> null
        }
    }
}

internal class SSUiFactoryImpl @Inject constructor() : SSUiFactory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is DashboardScreen -> ui<DashboardScreen.State> { state, modifier ->
                DashboardScreenUi(state, modifier)
            }

            is VideosScreen -> ui<VideosScreen.State> { state, modifier ->
                VideosUiScreen(
                    state,
                    modifier
                )
            }

            is VideoPlayerScreen -> ui<VideoPlayerScreen.State> { state, modifier ->
                VideoPlayerUiScreen(state, modifier)
            }

            else -> null
        }
    }
}
