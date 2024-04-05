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

package ss.lessons.impl.ext

import app.ss.models.LessonPdf
import app.ss.models.SSDay
import app.ss.models.SSLesson
import app.ss.models.SSLessonInfo
import ss.libraries.storage.api.entity.LessonEntity

internal fun SSLesson.toEntity(
    order: Int,
    days: List<SSDay> = emptyList(),
    pdfs: List<LessonPdf> = emptyList(),
): LessonEntity = LessonEntity(
    index = index,
    quarter = index.substringBeforeLast('-'),
    title = title,
    start_date = start_date,
    end_date = end_date,
    cover = cover,
    id = id,
    path = path,
    full_path = full_path,
    pdfOnly = pdfOnly,
    days = days,
    pdfs = pdfs,
    order = order,
)

internal fun LessonEntity.toInfoModel(): SSLessonInfo = SSLessonInfo(
    lesson = SSLesson(
        title = title,
        start_date = start_date,
        end_date = end_date,
        cover = cover,
        id = id,
        index = index,
        path = path,
        full_path = full_path,
        pdfOnly = pdfOnly
    ),
    days = days,
    pdfs = pdfs
)

internal fun LessonEntity.toModel(): SSLesson = SSLesson(
    index = index,
    title = title,
    start_date = start_date,
    end_date = end_date,
    cover = cover,
    id = id,
    path = path,
    full_path = full_path,
    pdfOnly = pdfOnly
)
