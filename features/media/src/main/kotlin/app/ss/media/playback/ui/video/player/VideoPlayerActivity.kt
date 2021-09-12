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

package app.ss.media.playback.ui.video.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.WindowInsetsControllerCompat
import app.ss.media.R
import app.ss.media.model.SSVideo
import app.ss.media.playback.players.SSVideoPlayer
import app.ss.media.playback.players.SSVideoPlayerImpl
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.fadeTo
import com.google.android.exoplayer2.ui.PlayerView

class VideoPlayerActivity : AppCompatActivity(R.layout.activity_video_player) {

    private lateinit var toolbar: Toolbar
    private lateinit var exoPlayerView: PlayerView
    private lateinit var composeView: ComposeView

    private val videoPlayer: SSVideoPlayer by lazy {
        SSVideoPlayerImpl(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar = findViewById(R.id.toolbar)
        exoPlayerView = findViewById(R.id.playerView)
        composeView = findViewById(R.id.composeView)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        exoPlayerView.overlayFrameLayout?.setOnClickListener {
            showSystemUI(findViewById(R.id.root))
        }

        composeView.setContent {
            VideoPlayerControls(
                videoPlayer = videoPlayer
            )
        }

        val video = intent.getParcelableExtra<SSVideo>(ARG_VIDEO)!!
        videoPlayer.playVideo(video, exoPlayerView)

        videoPlayer.playbackState.collectIn(this) { state ->
            if (state.isPlaying) {
                exoPlayerView.postDelayed(
                    {
                        hideSystemUI(findViewById(R.id.root))
                    },
                    HIDE_DELAY
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideSystemUI(view: View) {
        toolbar.fadeTo(false)
        composeView.fadeTo(false)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI(view: View) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, view).show(systemBars())
        toolbar.fadeTo(true)
        composeView.fadeTo(true)

        view.postDelayed(
            {
                if (videoPlayer.playbackState.value.isPlaying) {
                    hideSystemUI(view)
                }
            },
            HIDE_DELAY
        )
    }

    override fun onStop() {
        super.onStop()
        videoPlayer.onPause()
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.onResume()
    }

    override fun onDestroy() {
        videoPlayer.release()
        super.onDestroy()
    }

    companion object {
        private const val HIDE_DELAY = 2500L
        private const val ARG_VIDEO = "arg:video"

        fun launchIntent(
            context: Context,
            video: SSVideo
        ): Intent = Intent(
            context, VideoPlayerActivity::class.java
        ).apply {
            putExtra(ARG_VIDEO, video)
        }
    }
}
