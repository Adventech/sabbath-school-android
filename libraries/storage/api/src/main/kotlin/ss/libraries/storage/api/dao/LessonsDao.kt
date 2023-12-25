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

package ss.libraries.storage.api.dao

import androidx.room.Dao
import androidx.room.Query
import app.ss.models.LessonPdf
import app.ss.models.SSDay
import kotlinx.coroutines.flow.Flow
import ss.libraries.storage.api.entity.LessonEntity

@Dao
interface LessonsDao : BaseDao<LessonEntity> {

    @Query("SELECT * FROM lessons WHERE `index` = :lessonIndex")
    suspend fun get(lessonIndex: String): LessonEntity?

    @Query("SELECT * FROM lessons WHERE `index` = :lessonIndex")
    fun getAsFlow(lessonIndex: String): Flow<LessonEntity?>

    @Query("UPDATE lessons SET days = :days, pdfs = :pdfs, title = :title, cover = :cover, path = :path, full_path = :fullPath, pdfOnly = :pdfOnly WHERE `index` = :lessonIndex")
    fun updateInfo(
        lessonIndex: String,
        days: List<SSDay>,
        pdfs: List<LessonPdf>,
        title: String,
        cover: String,
        path: String,
        fullPath: String,
        pdfOnly: Boolean,
    )

    @Query("SELECT * FROM lessons")
    fun getAll(): Flow<List<LessonEntity>>

    @Query("DELETE FROM lessons")
    suspend fun deleteAll()
}
