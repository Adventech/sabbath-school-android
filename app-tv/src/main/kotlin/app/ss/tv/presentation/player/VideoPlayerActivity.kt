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
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import app.ss.models.media.SSVideo
import app.ss.tv.presentation.theme.SSTvTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ss.libraries.media.api.SSVideoPlayer
import javax.inject.Inject

@AndroidEntryPoint
class VideoPlayerActivity : ComponentActivity() {

    @Inject
    lateinit var circuit: Circuit

    @Inject
    lateinit var videoPlayer: SSVideoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val video = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(ARG_VIDEO, SSVideo::class.java) ?: return
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(ARG_VIDEO) ?: return
        }
        setContent {
            CircuitCompositionLocals(circuit = circuit) {
                SSTvTheme {
                    val backstack = rememberSaveableBackStack { push(VideoPlayerScreen(video)) }
                    BackHandler(onBack = { finishAfterTransition() })
                    val navigator = rememberCircuitNavigator(backstack)

                    NavigableCircuitContent(navigator, backstack)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            delay(PAUSE_DELAY)
            videoPlayer.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.onResume()
    }

    companion object {
        private const val ARG_VIDEO = "extra:video"
        private const val PAUSE_DELAY = 3000L

        fun launchIntent(
            context: Context,
            video: SSVideo,
        ): Intent = Intent(context, VideoPlayerActivity::class.java).apply {
            putExtra(ARG_VIDEO, video)
        }
    }
}
