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

package ss.lessons.test

import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import app.ss.models.LessonPdf
import ss.lessons.api.PdfReader
import ss.lessons.model.LocalFile

/** Fake implementation of [PdfReader] for use in tests. */
@VisibleForTesting
class FakePdfReader : PdfReader {

    private val localFiles: MutableSet<LocalFile> = mutableSetOf()

    override fun launchIntent(pdfs: List<LessonPdf>, lessonIndex: String): Intent = Intent()

    override suspend fun downloadFiles(pdfs: List<LessonPdf>): Result<List<LocalFile>> {
        val files = pdfs.map { LocalFile(it.title, Uri.parse(it.src)) }
        localFiles.addAll(files)

        return Result.success(files)
    }

    override fun isDownloaded(pdf: LessonPdf): Boolean {
        return localFiles.find { it.uri == Uri.parse(pdf.src) } != null
    }
}
