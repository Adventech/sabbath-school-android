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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.media.di

import app.ss.media.playback.AudioFocusHelper
import app.ss.media.playback.AudioFocusHelperImpl
import app.ss.media.playback.AudioQueueManager
import app.ss.media.playback.AudioQueueManagerImpl
import app.ss.media.playback.MediaNotifications
import app.ss.media.playback.MediaNotificationsImpl
import app.ss.media.playback.players.AudioPlayer
import app.ss.media.playback.players.AudioPlayerImpl
import app.ss.media.playback.players.SSAudioPlayer
import app.ss.media.playback.players.SSAudioPlayerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    internal abstract fun bindAudioPlayer(impl: AudioPlayerImpl): AudioPlayer

    @Binds
    internal abstract fun bindSSAudioPlayer(impl: SSAudioPlayerImpl): SSAudioPlayer

    @Binds
    internal abstract fun bindMediaNotifications(impl: MediaNotificationsImpl): MediaNotifications

    @Binds
    internal abstract fun bindAudioQueueManager(impl: AudioQueueManagerImpl): AudioQueueManager

    @Binds
    internal abstract fun bindAudioFocusHelper(impl: AudioFocusHelperImpl): AudioFocusHelper
}
