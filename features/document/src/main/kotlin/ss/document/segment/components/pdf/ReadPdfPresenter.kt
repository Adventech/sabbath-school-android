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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pspdfkit.annotations.Annotation
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.input.PDFAuxAnnotations
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ss.document.components.DocumentTopAppBarAction
import ss.foundation.coroutines.DispatcherProvider
import ss.libraries.circuit.navigation.AudioPlayerScreen
import ss.libraries.circuit.navigation.ExpandedAudioPlayerScreen
import ss.libraries.circuit.navigation.PdfScreen
import ss.libraries.circuit.navigation.VideosScreen
import ss.libraries.pdf.api.LocalFile
import ss.libraries.pdf.api.PdfReader
import ss.libraries.pdf.api.PdfReaderPrefs
import ss.resources.api.ResourcesRepository
import timber.log.Timber

class ReadPdfPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: PdfScreen,
    private val pdfReader: PdfReader,
    private val pdfReaderPrefs: PdfReaderPrefs,
    private val resourcesRepository: ResourcesRepository,
    private val topAppBarStateProducer: PdfTopAppBarStateProducer,
    private val dispatcherProvider: DispatcherProvider,
) : Presenter<ReadPdfState> {

    @Composable
    override fun present(): ReadPdfState {
        val documents by rememberFiles()
        val annotationsMap by rememberDocumentAnnotations()
        val topAppBarState = topAppBarStateProducer(screen)
        val config by rememberPdfReaderConfig()
        var overlayState by rememberRetained { mutableStateOf<ReadPdfOverlayState>(ReadPdfOverlayState.None) }
        val documentsState = rememberRetained(documents, annotationsMap) {
                documents.mapIndexed { index, file ->
                    val pdfId = screen.pdfs.getOrNull(index)?.id.orEmpty()
                    PdfDocumentState(
                        pdfId = pdfId,
                        file = file,
                        annotations = annotationsMap.getOrDefault(index, emptyList()).toImmutableList(),
                    )
                }.toImmutableList()
        }

        fun showAudioScreen() {
            overlayState = ReadPdfOverlayState.BottomSheet(
                screen = AudioPlayerScreen(resourceId = screen.resourceId, segmentId = screen.segmentId),
                skipPartiallyExpanded = true,
                onResult = { _ -> overlayState = ReadPdfOverlayState.None }
            )
        }

        fun showVideoScreen() {
            overlayState = ReadPdfOverlayState.BottomSheet(
                screen = VideosScreen(documentIndex = screen.documentIndex, documentId = screen.documentId),
                skipPartiallyExpanded = true,
                onResult = { _ -> overlayState = ReadPdfOverlayState.None }
            )
        }

        return when {
            documents.isNotEmpty() -> ReadPdfState.Success(
                documents = documentsState,
                topAppBarState = topAppBarState,
                config = config,
                overlayState = overlayState,
                eventSink = { event ->
                    when (event) {
                        ReadPdfEvent.OnNavBack -> navigator.pop()
                        is ReadPdfEvent.OnNavEvent -> {
                            when (val navEvent = event.event) {
                                is NavEvent.GoTo -> {
                                    if (navEvent.screen is ExpandedAudioPlayerScreen) {
                                        showAudioScreen()
                                    } else {
                                        navigator.goTo(navEvent.screen)
                                    }
                                }

                                else -> navigator.onNavEvent(navEvent)
                            }
                        }

                        is ReadPdfEvent.OnTopAppBarAction -> {
                            when (event.action) {
                                DocumentTopAppBarAction.Audio -> showAudioScreen()
                                DocumentTopAppBarAction.Video -> showVideoScreen()
                                // Everything else is not handled here
                                else -> Unit
                            }
                        }

                        is ReadPdfEvent.OnConfigurationChanged -> {
                            pdfReaderPrefs.saveConfiguration(event.config)

                            event.annotations?.let { saveAnnotations(pdfId = event.pdfId, annotations = it) }
                        }
                    }
                },
            )
            else -> ReadPdfState.Loading
        }
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

    @Composable
    private fun rememberPdfReaderConfig(): State<PdfReaderConfig> = rememberRetained {
        mutableStateOf(
            PdfReaderConfig(
                scrollMode = pdfReaderPrefs.scrollMode(),
                layoutMode = pdfReaderPrefs.pageLayoutMode(),
                scrollDirection = pdfReaderPrefs.scrollDirection(),
                themeMode = pdfReaderPrefs.themeMode(),
            )
        )
    }

    @Composable
    private fun rememberDocumentAnnotations(): State<Map<Int, List<PDFAuxAnnotations>>> = produceRetainedState(emptyMap()) {
        resourcesRepository.documentInput(screen.documentId)
            .map { userInputs ->
                userInputs.asSequence()
                    .mapNotNull { it as? UserInput.Annotation }
                    .toList()
            }
            .map { input ->
                val inputByPdfId = input.groupBy { it.pdfId }
                val pdfs = screen.pdfs
                pdfs.mapIndexed { index, pdf ->
                    index to (inputByPdfId[pdf.id]?.flatMap { it.data } ?: emptyList())
                }.toMap()
            }
            .catch { Timber.e(it) }
            .flowOn(dispatcherProvider.default)
            .collect { value = it }
    }

    private fun saveAnnotations(pdfId: String, annotations: List<Annotation>) {
        if (pdfId.isEmpty()) return

        val documentId = screen.documentId
        val syncAnnotations = annotations.toSync()

        val userInput = UserInputRequest.Annotation(
            blockId = pdfId,
            pdfId = pdfId,
            data = syncAnnotations
        )

        resourcesRepository.saveDocumentInput(documentId, userInput)
    }

    private fun List<Annotation>.toSync(): List<PDFAuxAnnotations> {
        val groupedAnnotations = groupBy { it.pageIndex }
        return groupedAnnotations.map { (pageIndex, list) ->
            val annotations = list.map { it.toInstantJson() }.filter(::isValidInstantJson)
            PDFAuxAnnotations(pageIndex, annotations)
        }
    }


    private fun isValidInstantJson(json: String) = json != "null"

    @CircuitInject(PdfScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: PdfScreen): ReadPdfPresenter
    }
}
