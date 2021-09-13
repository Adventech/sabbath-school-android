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

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.WindowInsetsControllerCompat
import app.ss.media.R
import app.ss.media.model.SSVideo
import app.ss.media.playback.BACKWARD
import app.ss.media.playback.FORWARD
import app.ss.media.playback.PLAY_PAUSE
import app.ss.media.playback.players.SSVideoPlayer
import app.ss.media.playback.players.SSVideoPlayerImpl
import app.ss.media.playback.players.hasEnded
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.fadeTo
import com.google.android.exoplayer2.ui.PlayerView

class VideoPlayerActivity : AppCompatActivity(R.layout.activity_video_player) {

    private lateinit var exoPlayerView: PlayerView
    private lateinit var composeView: ComposeView

    private val videoPlayer: SSVideoPlayer by lazy {
        SSVideoPlayerImpl(this)
    }

    private var systemUiVisible: Boolean = false
    private val pictureInPictureEnabled: Boolean
        get() {
            return if (supportsPnp) {
                isInPictureInPictureMode
            } else {
                false
            }
        }

    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action != ACTION_PIP_CONTROLS) {
                return
            }
            when (intent.getStringExtra(ACTION_TYPE)) {
                PLAY_PAUSE -> videoPlayer.playPause()
                BACKWARD -> videoPlayer.rewind()
                FORWARD -> videoPlayer.fastForward()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exoPlayerView = findViewById(R.id.playerView)
        composeView = findViewById(R.id.composeView)

        exoPlayerView.overlayFrameLayout?.setOnClickListener {
            if (systemUiVisible) {
                hideSystemUI()
            } else {
                showSystemUI()
            }
        }

        composeView.setContent {
            VideoPlayerControls(
                videoPlayer = videoPlayer,
                onClose = {
                    finish()
                }
            )
        }

        val video = intent.getParcelableExtra<SSVideo>(ARG_VIDEO) ?: run {
            finish()
            return
        }
        videoPlayer.playVideo(video, exoPlayerView)

        videoPlayer.playbackState.collectIn(this) { state ->
            if (state.isPlaying) {
                exoPlayerView.postDelayed(
                    {
                        hideSystemUI()
                    },
                    HIDE_DELAY
                )
            } else if (state.hasEnded) {
                showSystemUI()
            }

            if (supportsPnp && pictureInPictureEnabled) {
                setPictureInPictureParams(pictureInPictureParams(state.isPlaying))
            }
        }

        registerReceiver(broadcastReceiver, IntentFilter(ACTION_PIP_CONTROLS))
    }

    private fun hideSystemUI() {
        composeView.fadeTo(false)

        val view: View = findViewById(R.id.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        systemUiVisible = false
    }

    private fun showSystemUI() {
        if (pictureInPictureEnabled) return

        val view: View = findViewById(R.id.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, view).show(systemBars())
        composeView.fadeTo(true)

        view.postDelayed(
            {
                if (videoPlayer.playbackState.value.isPlaying) {
                    hideSystemUI()
                }
            },
            HIDE_DELAY
        )

        systemUiVisible = true
    }

    override fun onPause() {
        super.onPause()
        if (pictureInPictureEnabled.not()) {
            videoPlayer.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.onResume()
    }

    override fun onDestroy() {
        videoPlayer.release()
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (supportsPnp) {
            hideSystemUI()
            val params = pictureInPictureParams(videoPlayer.playbackState.value.isPlaying)
            enterPictureInPictureMode(params)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pictureInPictureParams(isPlaying: Boolean): PictureInPictureParams {
        val visibleRect = Rect()
        exoPlayerView.getGlobalVisibleRect(visibleRect)

        return PictureInPictureParams.Builder()
            .setActions(
                listOf(
                    createRemoteAction(
                        R.drawable.ic_audio_icon_backward,
                        "Rewind",
                        BACKWARD,
                        1
                    ),
                    createRemoteAction(
                        if (isPlaying) R.drawable.ic_audio_icon_pause else R.drawable.ic_audio_icon_play,
                        "Play/Pause",
                        PLAY_PAUSE,
                        2
                    ),
                    createRemoteAction(
                        R.drawable.ic_audio_icon_forward,
                        "Forward",
                        FORWARD,
                        3
                    ),
                )
            )
            .setAspectRatio(Rational(16, 9))
            .setSourceRectHint(visibleRect)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createRemoteAction(
        @DrawableRes iconResId: Int,
        title: String,
        action: String,
        requestCode: Int
    ): RemoteAction {
        return RemoteAction(
            Icon.createWithResource(this, iconResId),
            title,
            title,
            PendingIntent.getBroadcast(
                this,
                requestCode,
                Intent(ACTION_PIP_CONTROLS)
                    .putExtra(ACTION_TYPE, action),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    companion object {
        private const val HIDE_DELAY = 3500L
        private const val ARG_VIDEO = "arg:video"

        private const val ACTION_PIP_CONTROLS = "pip_media_controls"
        private const val ACTION_TYPE = "pip_media_action_type"

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

private val Context.supportsPnp: Boolean
    get() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }
