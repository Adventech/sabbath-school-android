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

package app.ss.storage.di

import android.content.Context
import app.ss.storage.db.SabbathSchoolDatabase
import app.ss.storage.db.dao.AudioDao
import app.ss.storage.db.dao.LanguagesDao
import app.ss.storage.db.dao.LessonsDao
import app.ss.storage.db.dao.PdfAnnotationsDao
import app.ss.storage.db.dao.QuarterliesDao
import app.ss.storage.db.dao.ReadCommentsDao
import app.ss.storage.db.dao.ReadHighlightsDao
import app.ss.storage.db.dao.ReadsDao
import app.ss.storage.db.dao.UserDao
import app.ss.storage.db.dao.VideoInfoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideAudioDao(
        @ApplicationContext context: Context,
    ): AudioDao = context.database().audioDao()

    @Provides
    @Singleton
    fun provideLanguagesDao(
        @ApplicationContext context: Context,
    ): LanguagesDao = context.database().languagesDao()

    @Provides
    @Singleton
    fun provideQuarterliesDao(
        @ApplicationContext context: Context,
    ): QuarterliesDao = context.database().quarterliesDao()

    @Provides
    @Singleton
    fun provideLessonsDao(
        @ApplicationContext context: Context,
    ): LessonsDao = context.database().lessonsDao()

    @Provides
    @Singleton
    fun provideReadsDao(
        @ApplicationContext context: Context,
    ): ReadsDao = context.database().readsDao()

    @Provides
    @Singleton
    fun providePdfAnnotationsDao(
        @ApplicationContext context: Context,
    ): PdfAnnotationsDao = context.database().pdfAnnotationsDao()

    @Provides
    @Singleton
    fun provideReadCommentsDao(
        @ApplicationContext context: Context,
    ): ReadCommentsDao = context.database().readCommentsDao()

    @Provides
    @Singleton
    fun provideReadHighlightsDao(
        @ApplicationContext context: Context,
    ): ReadHighlightsDao = context.database().readHighlightsDao()

    @Provides
    @Singleton
    fun provideUserDao(
        @ApplicationContext context: Context,
    ): UserDao = context.database().userDao()

    @Provides
    @Singleton
    fun provideVideoInfoDao(
        @ApplicationContext context: Context,
    ): VideoInfoDao = context.database().videoInfoDao()
}

private fun Context.database(): SabbathSchoolDatabase = SabbathSchoolDatabase.getInstance(this)
