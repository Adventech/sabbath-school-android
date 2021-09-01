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

package app.ss.media.playback

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
import android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
import android.media.AudioManager.FLAG_PLAY_SOUND
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.AudioManager.STREAM_MUSIC
import app.ss.media.playback.extensions.isOreo
import com.cryart.sabbathschool.core.extensions.context.systemService

typealias OnAudioFocusGain = AudioFocusHelper.() -> Unit
typealias OnAudioFocusLoss = AudioFocusHelper.() -> Unit
typealias OnAudioFocusLossTransient = AudioFocusHelper.() -> Unit
typealias OnAudioFocusLossTransientCanDuck = AudioFocusHelper.() -> Unit

interface AudioFocusHelper {
    var isAudioFocusGranted: Boolean

    fun requestPlayback(): Boolean
    fun abandonPlayback()
    fun onAudioFocusGain(audioFocusGain: OnAudioFocusGain)
    fun onAudioFocusLoss(audioFocusLoss: OnAudioFocusLoss)
    fun onAudioFocusLossTransient(audioFocusLossTransient: OnAudioFocusLossTransient)
    fun onAudioFocusLossTransientCanDuck(audioFocusLossTransientCanDuck: OnAudioFocusLossTransientCanDuck)
    fun setVolume(volume: Int)
}

internal class AudioFocusHelperImpl(
    context: Context
) : AudioFocusHelper, OnAudioFocusChangeListener {

    private val audioManager: AudioManager = context.systemService(AUDIO_SERVICE)

    private var audioFocusGainCallback: OnAudioFocusGain = {}
    private var audioFocusLossCallback: OnAudioFocusLoss = {}
    private var audioFocusLossTransientCallback: OnAudioFocusLossTransient = {}
    private var audioFocusLossTransientCanDuckCallback: OnAudioFocusLossTransientCanDuck = {}

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AUDIOFOCUS_GAIN -> audioFocusGainCallback()
            AUDIOFOCUS_LOSS -> audioFocusLossCallback()
            AUDIOFOCUS_LOSS_TRANSIENT -> audioFocusLossTransientCallback()
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> audioFocusLossTransientCanDuckCallback()
        }
    }

    override var isAudioFocusGranted: Boolean = false

    override fun requestPlayback(): Boolean {
        val state = if (isOreo()) {
            val attr = AudioAttributes.Builder().apply {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                setUsage(AudioAttributes.USAGE_MEDIA)
            }.build()
            audioManager.requestAudioFocus(
                AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attr)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()
            )
        } else {
            audioManager.requestAudioFocus(this, STREAM_MUSIC, AUDIOFOCUS_GAIN)
        }
        return state == AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun abandonPlayback() {
        if (isOreo()) {
            val attr = AudioAttributes.Builder().apply {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                setUsage(AudioAttributes.USAGE_MEDIA)
            }.build()

            audioManager.abandonAudioFocusRequest(
                AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(attr)
                    .build()
            )
        } else {
            audioManager.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusGain(audioFocusGain: OnAudioFocusGain) {
        audioFocusGainCallback = audioFocusGain
    }

    override fun onAudioFocusLoss(audioFocusLoss: OnAudioFocusLoss) {
        audioFocusLossCallback = audioFocusLoss
    }

    override fun onAudioFocusLossTransient(audioFocusLossTransient: OnAudioFocusLossTransient) {
        audioFocusLossTransientCallback = audioFocusLossTransient
    }

    override fun onAudioFocusLossTransientCanDuck(audioFocusLossTransientCanDuck: OnAudioFocusLossTransientCanDuck) {
        audioFocusLossTransientCanDuckCallback = audioFocusLossTransientCanDuck
    }

    override fun setVolume(volume: Int) {
        audioManager.adjustVolume(volume, FLAG_PLAY_SOUND)
    }
}
