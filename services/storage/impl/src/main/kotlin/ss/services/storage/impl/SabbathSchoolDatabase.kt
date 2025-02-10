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

package ss.services.storage.impl

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ss.libraries.storage.api.dao.AppWidgetDao
import ss.libraries.storage.api.dao.AudioDao
import ss.libraries.storage.api.dao.BibleVersionDao
import ss.libraries.storage.api.dao.DocumentsDao
import ss.libraries.storage.api.dao.FeedDao
import ss.libraries.storage.api.dao.FontFilesDao
import ss.libraries.storage.api.dao.LanguagesDao
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.PdfAnnotationsDao
import ss.libraries.storage.api.dao.PublishingInfoDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.libraries.storage.api.dao.ReadCommentsDao
import ss.libraries.storage.api.dao.ReadHighlightsDao
import ss.libraries.storage.api.dao.ReadsDao
import ss.libraries.storage.api.dao.ResourcesDao
import ss.libraries.storage.api.dao.SegmentsDao
import ss.libraries.storage.api.dao.UserDao
import ss.libraries.storage.api.dao.UserInputDao
import ss.libraries.storage.api.dao.VideoClipsDao
import ss.libraries.storage.api.dao.VideoInfoDao
import ss.libraries.storage.api.entity.AppWidgetEntity
import ss.libraries.storage.api.entity.AudioFileEntity
import ss.libraries.storage.api.entity.BibleVersionEntity
import ss.libraries.storage.api.entity.DocumentEntity
import ss.libraries.storage.api.entity.FeedEntity
import ss.libraries.storage.api.entity.UserInputEntity
import ss.libraries.storage.api.entity.FontFileEntity
import ss.libraries.storage.api.entity.LanguageEntity
import ss.libraries.storage.api.entity.LessonEntity
import ss.libraries.storage.api.entity.PdfAnnotationsEntity
import ss.libraries.storage.api.entity.PublishingInfoEntity
import ss.libraries.storage.api.entity.QuarterlyEntity
import ss.libraries.storage.api.entity.ReadCommentsEntity
import ss.libraries.storage.api.entity.ReadEntity
import ss.libraries.storage.api.entity.ReadHighlightsEntity
import ss.libraries.storage.api.entity.ResourceEntity
import ss.libraries.storage.api.entity.SegmentEntity
import ss.libraries.storage.api.entity.UserEntity
import ss.libraries.storage.api.entity.VideoClipEntity
import ss.libraries.storage.api.entity.VideoInfoEntity

@Database(
    entities = [
        AudioFileEntity::class,
        LanguageEntity::class,
        QuarterlyEntity::class,
        LessonEntity::class,
        ReadEntity::class,
        PdfAnnotationsEntity::class,
        ReadCommentsEntity::class,
        ReadHighlightsEntity::class,
        UserEntity::class,
        VideoClipEntity::class,
        VideoInfoEntity::class,
        PublishingInfoEntity::class,
        BibleVersionEntity::class,
        AppWidgetEntity::class,
        FontFileEntity::class,
        SegmentEntity::class,
        UserInputEntity::class,
        DocumentEntity::class,
        ResourceEntity::class,
        FeedEntity::class,
    ],
    version = 25,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 17, to = 18),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24),
        AutoMigration(from = 24, to = 25),
    ]
)
@TypeConverters(Converters::class, ResourcesConverters::class)
internal abstract class SabbathSchoolDatabase : RoomDatabase() {

    abstract fun audioDao(): AudioDao

    abstract fun languagesDao(): LanguagesDao

    abstract fun quarterliesDao(): QuarterliesDao

    abstract fun lessonsDao(): LessonsDao

    abstract fun readsDao(): ReadsDao

    abstract fun pdfAnnotationsDao(): PdfAnnotationsDao

    abstract fun readCommentsDao(): ReadCommentsDao

    abstract fun readHighlightsDao(): ReadHighlightsDao

    abstract fun videoClipsDao(): VideoClipsDao

    abstract fun videoInfoDao(): VideoInfoDao

    abstract fun userDao(): UserDao

    abstract fun publishingInfoDao(): PublishingInfoDao

    abstract fun bibleVersionDao(): BibleVersionDao

    abstract fun appWidgetDao(): AppWidgetDao

    abstract fun fontFilesDao(): FontFilesDao

    abstract fun segmentsDao(): SegmentsDao

    abstract fun userInputDao(): UserInputDao

    abstract fun documentsDao(): DocumentsDao

    abstract fun resourcesDao(): ResourcesDao

    abstract fun feedDao(): FeedDao

    companion object {
        private const val DATABASE_NAME = "sabbath_school_db"

        @Volatile
        private var INSTANCE: SabbathSchoolDatabase? = null

        fun getInstance(context: Context): SabbathSchoolDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): SabbathSchoolDatabase =
            Room.databaseBuilder(context, SabbathSchoolDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
