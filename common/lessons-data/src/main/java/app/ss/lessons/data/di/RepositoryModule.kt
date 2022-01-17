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

package app.ss.lessons.data.di

import app.ss.lessons.data.api.SSMediaApi
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.repository.lessons.LessonsRepositoryImpl
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.lessons.data.repository.media.MediaRepositoryImpl
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepositoryImpl
import app.ss.storage.db.dao.AudioDao
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideQuarterliesRepository(
        database: FirebaseDatabase,
        ssPrefs: SSPrefs
    ): QuarterliesRepository = QuarterliesRepositoryImpl(database, ssPrefs)

    @Provides
    @Singleton
    fun provideLessonsRepository(
        database: FirebaseDatabase,
        auth: FirebaseAuth,
        ssPrefs: SSPrefs
    ): LessonsRepository = LessonsRepositoryImpl(database, auth, ssPrefs)

    @Provides
    @Singleton
    fun provideMediaRepository(
        mediaApi: SSMediaApi,
        audioDao: AudioDao,
        schedulerProvider: SchedulerProvider
    ): MediaRepository =
        MediaRepositoryImpl(
            mediaApi = mediaApi,
            audioDao = audioDao,
            schedulerProvider = schedulerProvider,
        )
}
