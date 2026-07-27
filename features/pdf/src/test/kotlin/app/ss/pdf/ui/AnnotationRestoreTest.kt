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

package app.ss.pdf.ui

import io.adventech.blockkit.model.input.PDFAuxAnnotations
import io.adventech.blockkit.model.input.UserInput
import org.amshove.kluent.shouldBe
import org.junit.Assert.assertEquals
import org.junit.Test

class AnnotationRestoreTest {

    private val current = listOf(PDFAuxAnnotations(0, listOf("current")))
    private val candidate = listOf(PDFAuxAnnotations(0, listOf("valid", "corrupt")))

    @Test
    fun `lifecycle flush preserves descriptor indexes when an earlier PDF is not loaded`() {
        val flushed = mutableListOf<Pair<Int, String>>()

        listOf("first", null, "third").forEachIndexedPresent { index, document ->
            flushed += index to document
        }

        assertEquals(listOf(0 to "first", 2 to "third"), flushed)
    }

    @Test
    fun `annotation mapping distinguishes an absent record from an explicit empty snapshot`() {
        val explicitlyEmpty = UserInput.Annotation(
            blockId = "second",
            id = "input",
            timestamp = 1,
            pdfId = "second",
            data = emptyList(),
        )

        val mapped = mapAnnotationsByPdfId(
            pdfIds = listOf("first", "second"),
            inputs = listOf(explicitlyEmpty),
        )

        mapped.containsKey(0) shouldBe false
        mapped.containsKey(1) shouldBe true
        assertEquals(emptyList<PDFAuxAnnotations>(), mapped[1])
    }

    @Test
    fun `malformed payload restores the full pre-load snapshot instead of leaving a partial document`() {
        val provider = FakeAnnotationProvider(current)

        replaceAnnotationsSafely(
            current = provider::snapshot,
            clear = provider::clear,
            apply = provider::apply,
            candidate = candidate,
            fallback = null,
        ) shouldBe false

        assertEquals(current, provider.snapshot())
    }

    @Test
    fun `unreadable current snapshot rejects reload before destructive clear`() {
        var cleared = false
        var applied = false

        replaceAnnotationsSafely(
            current = { throw IllegalStateException("synthetic serialization failure") },
            clear = { cleared = true },
            apply = { applied = true },
            candidate = candidate,
            fallback = null,
        ) shouldBe false

        cleared shouldBe false
        applied shouldBe false
    }

    @Test
    fun `valid payload replaces the previous snapshot`() {
        val provider = FakeAnnotationProvider(current)
        val valid = listOf(PDFAuxAnnotations(1, listOf("replacement")))

        replaceAnnotationsSafely(
            current = provider::snapshot,
            clear = provider::clear,
            apply = provider::apply,
            candidate = valid,
            fallback = current,
        ) shouldBe true

        assertEquals(valid, provider.snapshot())
    }
}

private class FakeAnnotationProvider(initial: List<PDFAuxAnnotations>) {
    private val annotations = initial.flatMap { page ->
        page.annotations.map { page.pageIndex to it }
    }.toMutableList()

    fun snapshot(): List<PDFAuxAnnotations> = annotations
        .groupBy({ it.first }, { it.second })
        .map { (pageIndex, values) -> PDFAuxAnnotations(pageIndex, values) }

    fun clear() = annotations.clear()

    fun apply(snapshot: List<PDFAuxAnnotations>) {
        snapshot.forEach { page ->
            page.annotations.forEach { value ->
                if (value == "corrupt") throw IllegalArgumentException("synthetic malformed payload")
                annotations += page.pageIndex to value
            }
        }
    }
}
