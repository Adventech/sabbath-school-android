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

package ss.document.segment.components.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ss.libraries.circuit.navigation.PdfScreen
import ss.libraries.pdf.api.LocalFile
import ss.libraries.pdf.api.PdfReader

class ReadPdfPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: PdfScreen,
    private val pdfReader: PdfReader
) : Presenter<ReadPdfState> {

    @Composable
    override fun present(): ReadPdfState {
        val documents by rememberFiles()

        return ReadPdfState(
            documents = documents,
            eventSink = { event ->
                when (event) {
                    ReadPdfEvent.OpenPdf -> {
                        navigator.goTo(IntentScreen(pdfReader.launchIntent(screen)))
                    }
                }
            })
    }

    @Composable
    private fun rememberFiles(): State<ImmutableList<LocalFile>> = produceRetainedState<ImmutableList<LocalFile>>(persistentListOf()) {
        val result = pdfReader.downloadFiles(screen.pdfs)
        value = if (result.isSuccess) {
            result.getOrDefault(emptyList()).toImmutableList()
        } else {
            persistentListOf()
        }
    }

    @CircuitInject(PdfScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: PdfScreen): ReadPdfPresenter
    }
}
