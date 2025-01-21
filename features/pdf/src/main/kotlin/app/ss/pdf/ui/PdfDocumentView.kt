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

package app.ss.pdf.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.ss.pdf.model.LocalFile
import app.ss.pdf.model.PdfDocumentSpec
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.TabBarHidingMode
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.settings.SettingsMenuItemType
import com.pspdfkit.configuration.sharing.ShareFeatures
import com.pspdfkit.document.download.DownloadJob
import com.pspdfkit.document.download.DownloadRequest
import com.pspdfkit.jetpack.compose.interactors.DefaultListeners
import com.pspdfkit.jetpack.compose.interactors.getDefaultDocumentManager
import com.pspdfkit.jetpack.compose.interactors.rememberDocumentState
import com.pspdfkit.jetpack.compose.utilities.ExperimentalPSPDFKitApi
import com.pspdfkit.jetpack.compose.views.DocumentView
import com.slack.circuit.retained.produceRetainedState
import io.adventech.blockkit.model.resource.PdfAux
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.EnumSet
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPSPDFKitApi::class)
@Composable
fun PdfDocumentView(
    spec: PdfDocumentSpec,
    modifier: Modifier = Modifier
) {
    val documentState = rememberDocumentState(spec.file.uri, spec.pdfActivityConfiguration)

    DocumentView(
        documentState = documentState,
        modifier = modifier,
    )

}

@OptIn(ExperimentalPSPDFKitApi::class)
@Composable
fun PdfDocumentView(
    pdfAux: PdfAux,
    modifier: Modifier = Modifier
) {
    val file by rememberFile(LocalContext.current, pdfAux)
    val configuration = rememberConfiguration(LocalContext.current, pdfAux.title)

    if (file != null) {
        val documentState = rememberDocumentState(file!!.uri, configuration)

        DocumentView(
            documentState = documentState,
            modifier = modifier.padding(top = 68.dp),
            documentManager = getDefaultDocumentManager(
                documentListener = DefaultListeners.documentListeners(
                    onDocumentLoaded = {
                    }),
                uiListener = DefaultListeners.uiListeners(onImmersiveModeEnabled = { visibility ->
                    Timber.d("Immersive mode enabled: $visibility")
                })
            )
        )
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun rememberFile(context: Context, pdfAux: PdfAux) =
    produceRetainedState<LocalFile?>(null) {
        value = withContext(Dispatchers.IO) {
            downloadFile(context, pdfAux)
        }
    }

@Composable
private fun rememberConfiguration(context: Context, title: String) = remember {
    PdfActivityConfiguration.Builder(context)
        .fitMode(PageFitMode.FIT_TO_WIDTH)
        .animateScrollOnEdgeTaps(true)
        .setEnabledShareFeatures(EnumSet.noneOf(ShareFeatures::class.java))
        .setSettingsMenuItems(EnumSet.allOf(SettingsMenuItemType::class.java))
        .setTabBarHidingMode(TabBarHidingMode.HIDE)
        .title(title)
        .build()

}

private suspend fun downloadFile(
    context: Context,
    pdf: PdfAux
): LocalFile? = suspendCoroutine { continuation ->
    val outputFile = File(context.getDir("ssPdfs", Context.MODE_PRIVATE), "${pdf.id}.pdf")
    if (outputFile.exists()) {
        continuation.resume(LocalFile(pdf.title, Uri.fromFile(outputFile)))
        return@suspendCoroutine
    }

    val request: DownloadRequest = DownloadRequest.Builder(context)
        .uri(pdf.src)
        .outputFile(outputFile)
        .build()

    val downloadJob = DownloadJob.startDownload(request)
    downloadJob?.setProgressListener(object : DownloadJob.ProgressListenerAdapter() {
        override fun onComplete(output: File) {
            continuation.resume(LocalFile(pdf.title, Uri.fromFile(output)))
        }

        override fun onError(exception: Throwable) {
            Timber.e(exception)
            continuation.resume(null)
        }
    })
}
