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

package app.ss.pdf

import io.adventech.blockkit.model.input.PDFAuxAnnotations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class PdfAnnotationSaveQueue(
    private val scope: CoroutineScope,
    private val delayMillis: Long = DEFAULT_DELAY_MILLIS,
    private val save: (Pair<PdfAnnotationKey, List<PDFAuxAnnotations>>) -> Unit,
) {
    private val jobs = mutableMapOf<PdfAnnotationKey, Job>()

    fun enqueue(
        key: PdfAnnotationKey,
        annotations: List<PDFAuxAnnotations>,
    ) {
        jobs.remove(key)?.cancel()
        val snapshot = annotations.normalized()
        jobs[key] = scope.launch {
            delay(delayMillis)
            save(key to snapshot)
        }
    }

    fun flush(
        key: PdfAnnotationKey,
        annotations: List<PDFAuxAnnotations>,
    ) {
        jobs.remove(key)?.cancel()
        save(key to annotations.normalized())
    }

    private companion object {
        const val DEFAULT_DELAY_MILLIS = 350L
    }
}
