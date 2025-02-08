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

package ss.services.storage.impl.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ss.libraries.storage.api.dao.AppWidgetDao
import ss.libraries.storage.api.dao.AudioDao
import ss.libraries.storage.api.dao.BibleVersionDao
import ss.libraries.storage.api.dao.DocumentsDao
import ss.libraries.storage.api.dao.FontFilesDao
import ss.libraries.storage.api.dao.LanguagesDao
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.PdfAnnotationsDao
import ss.libraries.storage.api.dao.PublishingInfoDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.libraries.storage.api.dao.ReadCommentsDao
import ss.libraries.storage.api.dao.ReadHighlightsDao
import ss.libraries.storage.api.dao.ReadsDao
import ss.libraries.storage.api.dao.SegmentsDao
import ss.libraries.storage.api.dao.UserDao
import ss.libraries.storage.api.dao.UserInputDao
import ss.libraries.storage.api.dao.VideoClipsDao
import ss.libraries.storage.api.dao.VideoInfoDao
import ss.services.storage.impl.SabbathSchoolDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideAudioDao(
        @ApplicationContext context: Context
    ): AudioDao = context.database().audioDao()

    @Provides
    @Singleton
    fun provideLanguagesDao(
        @ApplicationContext context: Context
    ): LanguagesDao = context.database().languagesDao()

    @Provides
    @Singleton
    fun provideQuarterliesDao(
        @ApplicationContext context: Context
    ): QuarterliesDao = context.database().quarterliesDao()

    @Provides
    @Singleton
    fun provideLessonsDao(
        @ApplicationContext context: Context
    ): LessonsDao = context.database().lessonsDao()

    @Provides
    @Singleton
    fun provideReadsDao(
        @ApplicationContext context: Context
    ): ReadsDao = context.database().readsDao()

    @Provides
    @Singleton
    fun providePdfAnnotationsDao(
        @ApplicationContext context: Context
    ): PdfAnnotationsDao = context.database().pdfAnnotationsDao()

    @Provides
    @Singleton
    fun provideReadCommentsDao(
        @ApplicationContext context: Context
    ): ReadCommentsDao = context.database().readCommentsDao()

    @Provides
    @Singleton
    fun provideReadHighlightsDao(
        @ApplicationContext context: Context
    ): ReadHighlightsDao = context.database().readHighlightsDao()

    @Provides
    @Singleton
    fun provideUserDao(
        @ApplicationContext context: Context
    ): UserDao = context.database().userDao()

    @Provides
    @Singleton
    fun provideVideoInfoDao(
        @ApplicationContext context: Context
    ): VideoInfoDao = context.database().videoInfoDao()

    @Provides
    @Singleton
    fun provideVideoClipsDao(
        @ApplicationContext context: Context
    ): VideoClipsDao = context.database().videoClipsDao()


    @Provides
    @Singleton
    fun providePublishingInfoDao(
        @ApplicationContext context: Context
    ): PublishingInfoDao = context.database().publishingInfoDao()

    @Provides
    @Singleton
    fun provideBibleVersionDao(
        @ApplicationContext context: Context
    ): BibleVersionDao = context.database().bibleVersionDao()

    @Provides
    @Singleton
    fun provideAppWidgetDao(
        @ApplicationContext context: Context
    ): AppWidgetDao = context.database().appWidgetDao()

    @Provides
    @Singleton
    fun provideFontFilesDao(
        @ApplicationContext context: Context
    ): FontFilesDao = context.database().fontFilesDao()

    @Provides
    @Singleton
    fun provideSegmentsDao(
        @ApplicationContext context: Context
    ): SegmentsDao = context.database().segmentsDao()

    @Provides
    @Singleton
    fun provideUserInputDao(
        @ApplicationContext context: Context
    ): UserInputDao = context.database().userInputDao()

    @Provides
    @Singleton
    fun provideDocumentsDao(
        @ApplicationContext context: Context
    ): DocumentsDao = context.database().documentsDao()
}

private fun Context.database(): SabbathSchoolDatabase = SabbathSchoolDatabase.getInstance(this)
