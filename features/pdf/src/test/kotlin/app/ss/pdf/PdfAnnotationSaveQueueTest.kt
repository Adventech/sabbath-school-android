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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PdfAnnotationSaveQueueTest {

    private val firstKey = PdfAnnotationKey("document", "first-pdf")
    private val secondKey = PdfAnnotationKey("document", "second-pdf")
    private val first = listOf(PDFAuxAnnotations(0, listOf("first")))
    private val second = listOf(PDFAuxAnnotations(0, listOf("second")))

    @Test
    fun `debounce is isolated per PDF`() = runTest {
        val saves = mutableListOf<Pair<PdfAnnotationKey, List<PDFAuxAnnotations>>>()
        val queue = PdfAnnotationSaveQueue(this, delayMillis = 100, save = saves::add)

        queue.enqueue(firstKey, first)
        queue.enqueue(secondKey, second)
        advanceTimeBy(100)
        runCurrent()

        assertEquals(listOf(firstKey to first, secondKey to second), saves)
    }

    @Test
    fun `rapid updates coalesce to the newest complete snapshot`() = runTest {
        val saves = mutableListOf<Pair<PdfAnnotationKey, List<PDFAuxAnnotations>>>()
        val queue = PdfAnnotationSaveQueue(this, delayMillis = 100, save = saves::add)

        queue.enqueue(firstKey, first)
        queue.enqueue(firstKey, second)
        advanceTimeBy(100)
        runCurrent()

        assertEquals(listOf(firstKey to second), saves)
    }

    @Test
    fun `lifecycle flush persists immediately and cancels pending duplicate`() = runTest {
        val saves = mutableListOf<Pair<PdfAnnotationKey, List<PDFAuxAnnotations>>>()
        val queue = PdfAnnotationSaveQueue(this, delayMillis = 100, save = saves::add)

        queue.enqueue(firstKey, first)
        queue.flush(firstKey, second)
        advanceTimeBy(100)
        runCurrent()

        assertEquals(listOf(firstKey to second), saves)
    }
}
