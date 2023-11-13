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

package app.ss.lessons.data.repository.user

import app.ss.models.PdfAnnotations
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import kotlinx.coroutines.flow.Flow

/**
 * Responsible for user stored lesson data (Highlights, Comments, Annotations).
 */
interface UserDataRepository {

    fun getHighlights(readIndex: String): Flow<Result<SSReadHighlights>>

    fun saveHighlights(highlights: SSReadHighlights)

    fun getComments(readIndex: String): Flow<Result<SSReadComments>>

    fun saveComments(comments: SSReadComments)

    fun getAnnotations(lessonIndex: String, pdfId: String): Flow<Result<List<PdfAnnotations>>>

    fun saveAnnotations(lessonIndex: String, pdfId: String, annotations: List<PdfAnnotations>)

    /**
     * Clears all cached user data.
     */
    suspend fun clear()
}
