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

internal enum class PdfAnnotationRestoreSource {
    REMOTE,
    IN_MEMORY_DIRTY,
    JOURNAL_DIRTY,
}

internal data class PdfAnnotationRestorePlan(
    val annotations: List<PDFAuxAnnotations>,
    val fallback: List<PDFAuxAnnotations>?,
    val source: PdfAnnotationRestoreSource,
    val replayRequired: Boolean,
)

internal class PdfAnnotationSession(
    private val journal: PdfAnnotationJournal,
) {
    private val inMemoryDirty = mutableMapOf<PdfAnnotationKey, List<PDFAuxAnnotations>>()

    @Synchronized
    fun record(
        key: PdfAnnotationKey,
        annotations: List<PDFAuxAnnotations>,
    ): Boolean {
        val normalized = annotations.normalized()
        inMemoryDirty[key] = normalized
        return journal.write(key, normalized, dirty = true)
    }

    @Synchronized
    fun resolve(
        key: PdfAnnotationKey,
        remoteAnnotations: List<PDFAuxAnnotations>,
    ): PdfAnnotationRestorePlan {
        val remote = remoteAnnotations.normalized()
        inMemoryDirty[key]?.let { local ->
            if (local == remote && journal.write(key, local, dirty = false)) {
                inMemoryDirty.remove(key)
                return PdfAnnotationRestorePlan(
                    annotations = remote,
                    fallback = local,
                    source = PdfAnnotationRestoreSource.REMOTE,
                    replayRequired = false,
                )
            }
            return PdfAnnotationRestorePlan(
                annotations = local,
                fallback = remote,
                source = PdfAnnotationRestoreSource.IN_MEMORY_DIRTY,
                replayRequired = false,
            )
        }

        return when (val stored = journal.read(key)) {
            PdfAnnotationJournalRead.Missing,
            PdfAnnotationJournalRead.Corrupt,
            -> PdfAnnotationRestorePlan(
                annotations = remote,
                fallback = null,
                source = PdfAnnotationRestoreSource.REMOTE,
                replayRequired = false,
            )

            is PdfAnnotationJournalRead.Snapshot -> {
                val local = stored.annotations.normalized()
                if (!stored.dirty || local == remote) {
                    if (stored.dirty) journal.write(key, local, dirty = false)
                    PdfAnnotationRestorePlan(
                        annotations = remote,
                        fallback = local,
                        source = PdfAnnotationRestoreSource.REMOTE,
                        replayRequired = false,
                    )
                } else {
                    inMemoryDirty[key] = local
                    PdfAnnotationRestorePlan(
                        annotations = local,
                        fallback = remote,
                        source = PdfAnnotationRestoreSource.JOURNAL_DIRTY,
                        replayRequired = true,
                    )
                }
            }
        }
    }

    @Synchronized
    fun resolveAbsent(key: PdfAnnotationKey): PdfAnnotationRestorePlan? {
        inMemoryDirty[key]?.let { local ->
            return PdfAnnotationRestorePlan(
                annotations = local,
                fallback = null,
                source = PdfAnnotationRestoreSource.IN_MEMORY_DIRTY,
                replayRequired = false,
            )
        }

        val stored = journal.read(key) as? PdfAnnotationJournalRead.Snapshot ?: return null
        if (!stored.dirty) return null

        val local = stored.annotations.normalized()
        inMemoryDirty[key] = local
        return PdfAnnotationRestorePlan(
            annotations = local,
            fallback = null,
            source = PdfAnnotationRestoreSource.JOURNAL_DIRTY,
            replayRequired = true,
        )
    }
}
