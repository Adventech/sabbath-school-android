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

package app.ss.media.di

import android.content.ComponentName
import android.content.Context
import app.ss.media.playback.AudioFocusHelperImpl
import app.ss.media.playback.AudioQueueManagerImpl
import app.ss.media.playback.MediaNotifications
import app.ss.media.playback.MediaNotificationsImpl
import app.ss.media.playback.PlaybackConnection
import app.ss.media.playback.PlaybackConnectionImpl
import app.ss.media.playback.players.AudioPlayer
import app.ss.media.playback.players.AudioPlayerImpl
import app.ss.media.playback.players.SSAudioPlayer
import app.ss.media.playback.players.SSAudioPlayerImpl
import app.ss.media.playback.service.MusicService
import app.ss.media.repository.SSMediaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    @Singleton
    fun provideMediaNotifications(
        @ApplicationContext context: Context
    ): MediaNotifications = MediaNotificationsImpl(
        context
    )

    @Provides
    @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): AudioPlayer = AudioPlayerImpl(
        context,
        okHttpClient,
    )

    @Provides
    @Singleton
    fun provideSSPlayer(
        @ApplicationContext context: Context,
        player: AudioPlayer,
        repository: SSMediaRepository
    ): SSAudioPlayer = SSAudioPlayerImpl(
        context,
        player,
        audioFocusHelper = AudioFocusHelperImpl(context),
        queueManager = AudioQueueManagerImpl(repository),
        repository = repository
    )

    @Provides
    @Singleton
    fun playbackConnection(
        @ApplicationContext context: Context,
        audioPlayer: AudioPlayer,
    ): PlaybackConnection = PlaybackConnectionImpl(
        context = context,
        serviceComponent = ComponentName(context, MusicService::class.java),
        audioPlayer = audioPlayer,
    )
}
