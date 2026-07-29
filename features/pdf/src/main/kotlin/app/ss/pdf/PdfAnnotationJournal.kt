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

import android.content.Context
import app.ss.auth.PrivateUserDataCleaner
import dagger.hilt.android.qualifiers.ApplicationContext
import io.adventech.blockkit.model.input.PDFAuxAnnotations
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

internal data class PdfAnnotationKey(
    val documentId: String,
    val pdfId: String,
)

internal sealed interface PdfAnnotationJournalRead {
    data object Missing : PdfAnnotationJournalRead
    data object Corrupt : PdfAnnotationJournalRead

    data class Snapshot(
        val annotations: List<PDFAuxAnnotations>,
        val dirty: Boolean,
        val recoveredFromBackup: Boolean,
    ) : PdfAnnotationJournalRead
}

internal interface PdfAnnotationJournal {
    fun read(key: PdfAnnotationKey): PdfAnnotationJournalRead

    /**
     * Writes and fsyncs a complete, versioned snapshot before promoting it.
     * Returns false while retaining the previous known-good generation when a write fails.
     */
    fun write(
        key: PdfAnnotationKey,
        annotations: List<PDFAuxAnnotations>,
        dirty: Boolean,
    ): Boolean

    fun clear()
}

@Singleton
internal class FilePdfAnnotationJournal internal constructor(
    private val rootDirectory: File,
    private val beforePromote: () -> Unit = {},
) : PdfAnnotationJournal, PrivateUserDataCleaner {

    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this(File(context.noBackupFilesDir, DIRECTORY_NAME))

    @Synchronized
    override fun read(key: PdfAnnotationKey): PdfAnnotationJournalRead {
        val primary = primaryFile(key)
        decodeFile(primary)?.let { snapshot ->
            return snapshot.copy(recoveredFromBackup = false)
        }

        val backup = backupFile(key)
        decodeFile(backup)?.let { snapshot ->
            return snapshot.copy(recoveredFromBackup = true)
        }

        return if (primary.exists() || backup.exists()) {
            PdfAnnotationJournalRead.Corrupt
        } else {
            PdfAnnotationJournalRead.Missing
        }
    }

    @Synchronized
    override fun write(
        key: PdfAnnotationKey,
        annotations: List<PDFAuxAnnotations>,
        dirty: Boolean,
    ): Boolean {
        if (!ensureDirectory()) return false

        val snapshot = PdfAnnotationJournalRead.Snapshot(
            annotations = annotations.normalized(),
            dirty = dirty,
            recoveredFromBackup = false,
        )
        val primary = primaryFile(key)
        val backup = backupFile(key)
        val temporary = temporaryFile(key)

        return try {
            writeSynced(temporary, PdfAnnotationJournalCodec.encode(snapshot))
            if (decodeFile(temporary)?.copy(recoveredFromBackup = false) != snapshot) {
                return false
            }

            if (primary.exists()) {
                if (decodeFile(primary) != null) {
                    if (backup.exists() && !backup.delete()) return false
                    if (!primary.renameTo(backup)) return false
                } else if (!quarantine(primary)) {
                    return false
                }
            }

            beforePromote()
            temporary.renameTo(primary)
        } catch (_: IOException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    @Synchronized
    override fun clear() {
        if (!rootDirectory.exists()) return
        rootDirectory.listFiles().orEmpty().forEach { file ->
            if (file.isFile) file.delete()
        }
        rootDirectory.delete()
    }

    override fun clearPrivateUserData() = clear()

    internal fun primaryFile(key: PdfAnnotationKey): File =
        File(rootDirectory, "${key.fileName()}.journal")

    private fun backupFile(key: PdfAnnotationKey): File =
        File(rootDirectory, "${key.fileName()}.backup")

    private fun temporaryFile(key: PdfAnnotationKey): File =
        File(rootDirectory, "${key.fileName()}.temporary")

    private fun ensureDirectory(): Boolean = when {
        rootDirectory.isDirectory -> true
        rootDirectory.exists() -> false
        else -> rootDirectory.mkdirs()
    }

    private fun quarantine(file: File): Boolean {
        val quarantine = File(rootDirectory, "${file.name}.corrupt-${System.currentTimeMillis()}")
        return file.renameTo(quarantine)
    }

    private fun decodeFile(file: File): PdfAnnotationJournalRead.Snapshot? {
        if (!file.isFile || file.length() !in 1..MAX_FILE_BYTES) return null
        return try {
            PdfAnnotationJournalCodec.decode(file.readBytes())
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    private fun writeSynced(file: File, bytes: ByteArray) {
        FileOutputStream(file, false).use { stream ->
            stream.write(bytes)
            stream.flush()
            stream.fd.sync()
        }
    }

    private fun PdfAnnotationKey.fileName(): String {
        val input = "$documentId\u0000$pdfId".toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance(SHA_256)
            .digest(input)
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private companion object {
        const val DIRECTORY_NAME = "pdf-annotation-journal-v1"
        const val SHA_256 = "SHA-256"
        const val MAX_FILE_BYTES = 32L * 1024L * 1024L
    }
}

private object PdfAnnotationJournalCodec {
    private const val MAGIC = 0x53535041
    private const val VERSION = 1
    private const val CHECKSUM_BYTES = 32
    private const val MAX_PAGES = 100_000
    private const val MAX_ANNOTATIONS = 1_000_000
    private const val MAX_JSON_BYTES = 4 * 1024 * 1024
    private const val MAX_ENCODED_BYTES = 32 * 1024 * 1024

    fun encode(snapshot: PdfAnnotationJournalRead.Snapshot): ByteArray {
        require(snapshot.annotations.size <= MAX_PAGES)
        var annotationCount = 0
        val payload = ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(MAGIC)
                output.writeInt(VERSION)
                output.writeBoolean(snapshot.dirty)
                output.writeInt(snapshot.annotations.size)
                snapshot.annotations.forEach { page ->
                    require(page.pageIndex >= 0)
                    annotationCount += page.annotations.size
                    require(annotationCount <= MAX_ANNOTATIONS)
                    output.writeInt(page.pageIndex)
                    output.writeInt(page.annotations.size)
                    page.annotations.forEach { json ->
                        val encoded = json.toByteArray(Charsets.UTF_8)
                        require(encoded.size <= MAX_JSON_BYTES)
                        require(bytes.size() + encoded.size + Int.SIZE_BYTES + CHECKSUM_BYTES <= MAX_ENCODED_BYTES)
                        output.writeInt(encoded.size)
                        output.write(encoded)
                    }
                }
            }
            bytes.toByteArray()
        }
        require(payload.size + CHECKSUM_BYTES <= MAX_ENCODED_BYTES)
        val checksum = MessageDigest.getInstance("SHA-256").digest(payload)
        return payload + checksum
    }

    fun decode(bytes: ByteArray): PdfAnnotationJournalRead.Snapshot? {
        if (bytes.size <= CHECKSUM_BYTES) return null
        val payload = bytes.copyOfRange(0, bytes.size - CHECKSUM_BYTES)
        val actualChecksum = bytes.copyOfRange(bytes.size - CHECKSUM_BYTES, bytes.size)
        val expectedChecksum = MessageDigest.getInstance("SHA-256").digest(payload)
        if (!MessageDigest.isEqual(actualChecksum, expectedChecksum)) return null

        return try {
            DataInputStream(ByteArrayInputStream(payload)).use { input ->
                if (input.readInt() != MAGIC || input.readInt() != VERSION) return null
                val dirty = input.readBoolean()
                val pageCount = input.readInt()
                if (pageCount !in 0..MAX_PAGES) return null

                var annotationCount = 0
                val pages = buildList(pageCount) {
                    repeat(pageCount) {
                        val pageIndex = input.readInt()
                        val pageAnnotationCount = input.readInt()
                        if (pageIndex < 0 || pageAnnotationCount !in 0..MAX_ANNOTATIONS) return null
                        annotationCount += pageAnnotationCount
                        if (annotationCount > MAX_ANNOTATIONS) return null
                        val annotations = buildList(pageAnnotationCount) {
                            repeat(pageAnnotationCount) {
                                val length = input.readInt()
                                if (length !in 0..MAX_JSON_BYTES || length > input.available()) return null
                                val encoded = ByteArray(length)
                                input.readFully(encoded)
                                add(encoded.toString(Charsets.UTF_8))
                            }
                        }
                        add(PDFAuxAnnotations(pageIndex, annotations))
                    }
                }
                if (input.available() != 0) return null
                PdfAnnotationJournalRead.Snapshot(
                    annotations = pages.normalized(),
                    dirty = dirty,
                    recoveredFromBackup = false,
                )
            }
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

internal fun List<PDFAuxAnnotations>.normalized(): List<PDFAuxAnnotations> =
    groupBy { it.pageIndex }
        .toSortedMap()
        .map { (pageIndex, pages) ->
            PDFAuxAnnotations(pageIndex, pages.flatMap { it.annotations })
        }
