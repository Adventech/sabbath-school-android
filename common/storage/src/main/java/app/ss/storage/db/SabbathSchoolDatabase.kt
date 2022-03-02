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

package app.ss.storage.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import app.ss.storage.db.entity.AudioFileEntity
import app.ss.storage.db.entity.LanguageEntity
import app.ss.storage.db.entity.LessonEntity
import app.ss.storage.db.entity.PdfAnnotationsEntity
import app.ss.storage.db.entity.QuarterlyEntity
import app.ss.storage.db.entity.ReadCommentsEntity
import app.ss.storage.db.entity.ReadEntity
import app.ss.storage.db.entity.ReadHighlightsEntity
import app.ss.storage.db.entity.UserEntity
import app.ss.storage.db.entity.VideoInfoEntity

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
        VideoInfoEntity::class
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6)
    ]
)
@TypeConverters(Converters::class)
internal abstract class SabbathSchoolDatabase : RoomDatabase() {

    abstract fun audioDao(): AudioDao

    abstract fun languagesDao(): LanguagesDao

    abstract fun quarterliesDao(): QuarterliesDao

    abstract fun lessonsDao(): LessonsDao

    abstract fun readsDao(): ReadsDao

    abstract fun pdfAnnotationsDao(): PdfAnnotationsDao

    abstract fun readCommentsDao(): ReadCommentsDao

    abstract fun readHighlightsDao(): ReadHighlightsDao

    abstract fun videoInfoDao(): VideoInfoDao

    abstract fun userDao(): UserDao

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
