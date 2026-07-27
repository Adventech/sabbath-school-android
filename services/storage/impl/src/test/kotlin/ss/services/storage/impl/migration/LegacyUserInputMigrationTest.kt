/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

package ss.services.storage.impl.migration

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import ss.services.storage.impl.SabbathSchoolDatabase

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class LegacyUserInputMigrationTest {

    private val testDatabase: String by lazy {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getDatabasePath(TEST_DATABASE_FILENAME)
            .absolutePath
    }

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SabbathSchoolDatabase::class.java,
    )

    @Test
    fun `migration preserves legacy payloads until user input is cleared`() {
        migrationHelper.createDatabase(testDatabase, 26).use { database ->
            database.seedVersion26UserInput()
        }

        val migrated = migrationHelper.runMigrationsAndValidate(
            testDatabase,
            CURRENT_DATABASE_VERSION,
            true,
            LegacyUserInputMigration,
        )

        migrated.use { database ->
            database.assertLegacyAnnotationsPreserved()
            database.assertLegacyCommentsPreserved()
            database.assertLegacyHighlightsPreserved()
            database.assertCurrentUserInputPreserved()
            assertFalse(database.hasTable("annotations"))
            assertFalse(database.hasTable("comments"))
            assertFalse(database.hasTable("highlights"))
        }

        val database = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            SabbathSchoolDatabase::class.java,
            testDatabase,
        )
            .addMigrations(LegacyUserInputMigration)
            .allowMainThreadQueries()
            .build()
        try {
            database.userInputDao().clear()
            database.openHelper.writableDatabase.assertAllUserInputCleared()
        } finally {
            database.close()
        }
    }

    private fun SupportSQLiteDatabase.seedVersion26UserInput() {
        execSQL(
            """
            INSERT INTO annotations (`index`, `pdfIndex`, `pageIndex`, `annotations`, `timestamp`)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any>(ANNOTATION_INDEX, PDF_INDEX, PAGE_INDEX, ANNOTATIONS_PAYLOAD, ANNOTATIONS_TIMESTAMP),
        )
        execSQL(
            """
            INSERT INTO comments (`readIndex`, `comments`, `timestamp`)
            VALUES (?, ?, ?)
            """.trimIndent(),
            arrayOf<Any>(READ_INDEX, COMMENTS_PAYLOAD, COMMENTS_TIMESTAMP),
        )
        execSQL(
            """
            INSERT INTO highlights (`readIndex`, `highlights`, `timestamp`)
            VALUES (?, ?, ?)
            """.trimIndent(),
            arrayOf<Any>(READ_INDEX, HIGHLIGHTS_PAYLOAD, HIGHLIGHTS_TIMESTAMP),
        )
        execSQL(
            """
            INSERT INTO user_input (`localId`, `id`, `documentId`, `input`, `timestamp`)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any>(USER_INPUT_LOCAL_ID, USER_INPUT_ID, DOCUMENT_ID, USER_INPUT_PAYLOAD, USER_INPUT_TIMESTAMP),
        )
    }

    private fun SupportSQLiteDatabase.assertLegacyAnnotationsPreserved() {
        query(
            """
            SELECT `index`, `pdfIndex`, `pageIndex`, `annotations`, `timestamp`
            FROM `legacy_annotations_v26`
            """.trimIndent()
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(ANNOTATION_INDEX, cursor.getString(0))
            assertEquals(PDF_INDEX, cursor.getString(1))
            assertEquals(PAGE_INDEX, cursor.getInt(2))
            assertEquals(ANNOTATIONS_PAYLOAD, cursor.getString(3))
            assertEquals(ANNOTATIONS_TIMESTAMP, cursor.getLong(4))
            assertFalse(cursor.moveToNext())
        }
    }

    private fun SupportSQLiteDatabase.assertLegacyCommentsPreserved() {
        query(
            """
            SELECT `readIndex`, `comments`, `timestamp`
            FROM `legacy_comments_v26`
            """.trimIndent()
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(READ_INDEX, cursor.getString(0))
            assertEquals(COMMENTS_PAYLOAD, cursor.getString(1))
            assertEquals(COMMENTS_TIMESTAMP, cursor.getLong(2))
            assertFalse(cursor.moveToNext())
        }
    }

    private fun SupportSQLiteDatabase.assertLegacyHighlightsPreserved() {
        query(
            """
            SELECT `readIndex`, `highlights`, `timestamp`
            FROM `legacy_highlights_v26`
            """.trimIndent()
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(READ_INDEX, cursor.getString(0))
            assertEquals(HIGHLIGHTS_PAYLOAD, cursor.getString(1))
            assertEquals(HIGHLIGHTS_TIMESTAMP, cursor.getLong(2))
            assertFalse(cursor.moveToNext())
        }
    }

    private fun SupportSQLiteDatabase.assertCurrentUserInputPreserved() {
        query(
            """
            SELECT `localId`, `id`, `documentId`, `input`, `timestamp`
            FROM `user_input`
            """.trimIndent()
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(USER_INPUT_LOCAL_ID, cursor.getString(0))
            assertEquals(USER_INPUT_ID, cursor.getString(1))
            assertEquals(DOCUMENT_ID, cursor.getString(2))
            assertEquals(USER_INPUT_PAYLOAD, cursor.getString(3))
            assertEquals(USER_INPUT_TIMESTAMP, cursor.getLong(4))
            assertFalse(cursor.moveToNext())
        }
    }

    private fun SupportSQLiteDatabase.assertAllUserInputCleared() {
        assertEquals(0, rowCount("user_input"))
        assertEquals(0, rowCount("legacy_annotations_v26"))
        assertEquals(0, rowCount("legacy_comments_v26"))
        assertEquals(0, rowCount("legacy_highlights_v26"))
    }

    private fun SupportSQLiteDatabase.rowCount(tableName: String): Int =
        query("SELECT COUNT(*) FROM `$tableName`").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun SupportSQLiteDatabase.hasTable(tableName: String): Boolean =
        query(
            "SELECT EXISTS(SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?)",
            arrayOf(tableName),
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0) == 1
        }

    private companion object {
        const val TEST_DATABASE_FILENAME = "legacy-user-input-migration-test"
        const val CURRENT_DATABASE_VERSION = 33
        const val ANNOTATION_INDEX = "2025-q1-lesson-1-en-pdf-a-7"
        const val PDF_INDEX = "2025-q1-lesson-1-en-pdf-a"
        const val PAGE_INDEX = 7
        const val ANNOTATIONS_PAYLOAD = """["annotation naïve Библия 漢字 \"quoted\""]"""
        const val ANNOTATIONS_TIMESTAMP = 1_735_689_601_001L
        const val READ_INDEX = "en-2025-01-01-ß"
        const val COMMENTS_PAYLOAD =
            """[{"elementId":"question-Ț-1","comment":"Б & <tag> — keep offline"}]"""
        const val COMMENTS_TIMESTAMP = 1_735_689_602_002L
        const val HIGHLIGHTS_PAYLOAD =
            """{"version":1,"highlights":["a[href='#Б']","span[data-id='漢字']"]}"""
        const val HIGHLIGHTS_TIMESTAMP = 1_735_689_603_003L
        const val USER_INPUT_LOCAL_ID = "document-current-comments-block-current"
        const val USER_INPUT_ID = "remote-input-id"
        const val DOCUMENT_ID = "document-current"
        const val USER_INPUT_PAYLOAD =
            """{"inputType":"comments","blockId":"block-current","id":"remote-input-id","timestamp":1735689604004,"comment":"already migrated"}"""
        const val USER_INPUT_TIMESTAMP = 1_735_689_604_004L
    }
}
