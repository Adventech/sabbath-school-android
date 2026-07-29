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

class PdfAnnotationSessionTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val key = PdfAnnotationKey(documentId = "document", pdfId = "pdf")
    private val stale = listOf(PDFAuxAnnotations(0, listOf("{\"uuid\":\"stale\"}")))
    private val local = listOf(PDFAuxAnnotations(0, listOf("{\"uuid\":\"local\"}")))
    private val newerRemote = listOf(PDFAuxAnnotations(0, listOf("{\"uuid\":\"newer\"}")))

    @Test
    fun `late repository emission cannot overwrite an in-memory dirty snapshot`() {
        val session = PdfAnnotationSession(FakePdfAnnotationJournal())

        session.record(key, local) shouldBe true

        assertEquals(
            PdfAnnotationRestorePlan(
                annotations = local,
                fallback = stale,
                source = PdfAnnotationRestoreSource.IN_MEMORY_DIRTY,
                replayRequired = false,
            ),
            session.resolve(key, stale),
        )
    }

    @Test
    fun `process restart restores dirty journal and requests persistence replay`() {
        val directory = temporaryFolder.newFolder()
        PdfAnnotationSession(FilePdfAnnotationJournal(directory)).record(key, local) shouldBe true

        val restarted = PdfAnnotationSession(FilePdfAnnotationJournal(directory))

        assertEquals(
            PdfAnnotationRestorePlan(
                annotations = local,
                fallback = stale,
                source = PdfAnnotationRestoreSource.JOURNAL_DIRTY,
                replayRequired = true,
            ),
            restarted.resolve(key, stale),
        )
    }

    @Test
    fun `process restart restores dirty journal before repository emits`() {
        val directory = temporaryFolder.newFolder()
        PdfAnnotationSession(FilePdfAnnotationJournal(directory)).record(key, local) shouldBe true

        val restarted = PdfAnnotationSession(FilePdfAnnotationJournal(directory))

        assertEquals(
            PdfAnnotationRestorePlan(
                annotations = local,
                fallback = null,
                source = PdfAnnotationRestoreSource.JOURNAL_DIRTY,
                replayRequired = true,
            ),
            restarted.resolveAbsent(key),
        )
    }

    @Test
    fun `matching durable repository snapshot compacts dirty journal without changing wire data`() {
        val directory = temporaryFolder.newFolder()
        PdfAnnotationSession(FilePdfAnnotationJournal(directory)).record(key, local) shouldBe true
        val restarted = PdfAnnotationSession(FilePdfAnnotationJournal(directory))

        assertEquals(local, restarted.resolve(key, local).annotations)

        val afterCompaction = PdfAnnotationSession(FilePdfAnnotationJournal(directory))
        assertEquals(
            PdfAnnotationRestorePlan(
                annotations = newerRemote,
                fallback = local,
                source = PdfAnnotationRestoreSource.REMOTE,
                replayRequired = false,
            ),
            afterCompaction.resolve(key, newerRemote),
        )
    }

    @Test
    fun `failed disk write still guards dirty ink for the current process`() {
        val session = PdfAnnotationSession(FakePdfAnnotationJournal(writeSucceeds = false))

        session.record(key, local) shouldBe false

        assertEquals(local, session.resolve(key, stale).annotations)
        session.resolve(key, stale).source shouldBe PdfAnnotationRestoreSource.IN_MEMORY_DIRTY
    }
}

private class FakePdfAnnotationJournal(
    private val writeSucceeds: Boolean = true,
) : PdfAnnotationJournal {
    private val entries = mutableMapOf<PdfAnnotationKey, PdfAnnotationJournalRead.Snapshot>()

    override fun read(key: PdfAnnotationKey): PdfAnnotationJournalRead =
        entries[key] ?: PdfAnnotationJournalRead.Missing

    override fun write(
        key: PdfAnnotationKey,
        annotations: List<PDFAuxAnnotations>,
        dirty: Boolean,
    ): Boolean {
        if (writeSucceeds) {
            entries[key] = PdfAnnotationJournalRead.Snapshot(annotations, dirty, false)
        }
        return writeSucceeds
    }

    override fun clear() = entries.clear()
}
