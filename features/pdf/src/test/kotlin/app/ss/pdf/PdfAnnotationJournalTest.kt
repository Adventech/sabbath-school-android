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
import org.amshove.kluent.shouldBe
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class PdfAnnotationJournalTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val key = PdfAnnotationKey(documentId = "document", pdfId = "pdf")
    private val first = listOf(PDFAuxAnnotations(0, listOf("{\"uuid\":\"first\"}")))
    private val second = listOf(PDFAuxAnnotations(1, listOf("{\"uuid\":\"second\"}")))

    @Test
    fun `dirty snapshot survives a new journal instance`() {
        val directory = temporaryFolder.newFolder()
        val firstProcess = FilePdfAnnotationJournal(directory)

        firstProcess.write(key, first, dirty = true) shouldBe true

        val afterProcessDeath = FilePdfAnnotationJournal(directory).read(key)
        assertEquals(
            PdfAnnotationJournalRead.Snapshot(
                annotations = first,
                dirty = true,
                recoveredFromBackup = false,
            ),
            afterProcessDeath,
        )
    }

    @Test
    fun `interrupted promotion preserves the previous known-good generation`() {
        val directory = temporaryFolder.newFolder()
        FilePdfAnnotationJournal(directory).write(key, first, dirty = true) shouldBe true
        val interrupted = FilePdfAnnotationJournal(
            rootDirectory = directory,
            beforePromote = { throw IOException("synthetic promotion failure") },
        )

        interrupted.write(key, second, dirty = true) shouldBe false

        assertEquals(
            PdfAnnotationJournalRead.Snapshot(
                annotations = first,
                dirty = true,
                recoveredFromBackup = true,
            ),
            FilePdfAnnotationJournal(directory).read(key),
        )
    }

    @Test
    fun `corrupt primary falls back to previous generation without deleting evidence`() {
        val directory = temporaryFolder.newFolder()
        val journal = FilePdfAnnotationJournal(directory)
        journal.write(key, first, dirty = true) shouldBe true
        journal.write(key, second, dirty = true) shouldBe true
        val primary = journal.primaryFile(key)
        primary.writeBytes(byteArrayOf(1, 2, 3))

        assertEquals(
            PdfAnnotationJournalRead.Snapshot(
                annotations = first,
                dirty = true,
                recoveredFromBackup = true,
            ),
            journal.read(key),
        )
        primary.exists() shouldBe true
    }

    @Test
    fun `corrupt only generation is reported and retained`() {
        val directory = temporaryFolder.newFolder()
        val journal = FilePdfAnnotationJournal(directory)
        journal.write(key, first, dirty = true) shouldBe true
        val primary = journal.primaryFile(key)
        primary.writeBytes(byteArrayOf(1, 2, 3))

        journal.read(key) shouldBe PdfAnnotationJournalRead.Corrupt
        primary.exists() shouldBe true
    }

    @Test
    fun `private journal generations are removed on user-data clear`() {
        val directory = temporaryFolder.newFolder()
        val journal = FilePdfAnnotationJournal(directory)
        journal.write(key, first, dirty = true) shouldBe true
        journal.write(key, second, dirty = false) shouldBe true

        journal.clearPrivateUserData()

        journal.read(key) shouldBe PdfAnnotationJournalRead.Missing
        directory.exists() shouldBe false
    }
}
