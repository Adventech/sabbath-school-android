/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.PdfAux
import app.ss.pdf.model.LocalFile
import app.ss.pdf.model.PdfDocumentSpec
import ss.libraries.circuit.navigation.PdfScreen

class ReadPdfPresenter @AssistedInject constructor(
    @Assisted private val screen: PdfScreen,
    private val pdfReader: PdfReader
) : Presenter<ReadPdfState>{

    @Composable
    override fun present(): ReadPdfState {
        val configuration = remember { pdfReader.configuration() }

        val result by rememberFile()
        val files = result

        return when {
            files.isNullOrEmpty() -> ReadPdfState.Loading
            else -> {
                val file = files.first()
                ReadPdfState.Success(
                    documentSpec = PdfDocumentSpec(
                        pdfActivityConfiguration = configuration,
                        file = file,
                    )
                )
            }
        }
    }

    @Composable
    private fun rememberFile() = produceState<List<LocalFile>?>(initialValue = null) {
        value = pdfReader.downloadFiles(
            pdfs = screen.pdfs.map {
                PdfAux(
                    id = it.id,
                    src = it.url,
                    title = it.title,
                    target = null,
                    targetIndex = null,
                )
            }
        ).getOrNull()
    }

    @CircuitInject(PdfScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(screen: PdfScreen): ReadPdfPresenter
    }
}
