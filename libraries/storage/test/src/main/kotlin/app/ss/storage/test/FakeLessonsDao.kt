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

package app.ss.storage.test

import androidx.annotation.VisibleForTesting
import app.ss.models.LessonPdf
import app.ss.models.SSDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.entity.LessonEntity

/** Fake implementation of [LessonsDao] for use in tests. */
@VisibleForTesting
class FakeLessonsDao : LessonsDao {

    private val lessonsFlow = MutableStateFlow<List<LessonEntity>>(emptyList())

    fun addLesson(lesson: LessonEntity) {
        lessonsFlow.update { (it + lesson).toSet().toList() }
    }

    override suspend fun get(lessonIndex: String): LessonEntity? {
        return lessonsFlow.firstOrNull()?.find { it.index == lessonIndex }
    }

    override fun getAsFlow(lessonIndex: String): Flow<LessonEntity?> =
        lessonsFlow.map { entities -> entities.firstOrNull { it.index == lessonIndex } }

    override fun updateInfo(
        lessonIndex: String,
        days: List<SSDay>,
        pdfs: List<LessonPdf>,
        title: String,
        cover: String,
        path: String,
        fullPath: String,
        pdfOnly: Boolean
    ) = Unit

    override fun getAll(): Flow<List<LessonEntity>> = lessonsFlow

    override suspend fun deleteAll() = Unit

    override suspend fun insertItem(item: LessonEntity) = Unit

    override suspend fun insertAll(items: List<LessonEntity>) = Unit

    override suspend fun update(item: LessonEntity) = Unit
}
