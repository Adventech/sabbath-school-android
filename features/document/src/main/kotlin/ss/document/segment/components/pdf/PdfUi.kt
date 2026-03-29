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

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.UserInterfaceViewMode
import com.pspdfkit.configuration.theming.ThemeMode
import com.pspdfkit.jetpack.compose.interactors.getDefaultDocumentManager
import com.pspdfkit.jetpack.compose.interactors.rememberDocumentState
import com.pspdfkit.jetpack.compose.views.DocumentView
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyle
import ss.libraries.pdf.api.LocalFile
import ss.document.R as DocumentR

@Composable
fun PdfUi(document: LocalFile, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val documentUri = document.uri
    val readerStyle = LocalReaderStyle.current
    // 1. Resolve the mapped theme ID for UI
    val themeResId = readerStyle.theme.toPdfThemeResId()

    // 2. Resolve ThemeMode for PSPDFKit's built-in document inversion
    val themeMode = when (readerStyle.theme) {
        ReaderStyle.Theme.Dark -> ThemeMode.NIGHT
        ReaderStyle.Theme.Auto -> if (isSystemInDarkTheme()) ThemeMode.NIGHT else ThemeMode.DEFAULT
        else -> ThemeMode.DEFAULT
    }
    val pdfActivityConfiguration = rememberPdfConfiguration(context, document, themeResId, themeMode)

    val documentState = rememberDocumentState(documentUri, pdfActivityConfiguration)

    DocumentView(
        documentState = documentState,
        modifier = modifier,
        documentManager = getDefaultDocumentManager()
    )
}

@Composable
fun ReaderStyle.Theme.toPdfThemeResId(): Int {
    return when (this) {
        ReaderStyle.Theme.Light -> DocumentR.style.Theme_SS_Pdf_Light
        ReaderStyle.Theme.Dark -> DocumentR.style.Theme_SS_Pdf_Dark
        ReaderStyle.Theme.Sepia -> DocumentR.style.Theme_SS_Pdf_Sepia
        ReaderStyle.Theme.Auto -> {
            if (isSystemInDarkTheme()) DocumentR.style.Theme_SS_Pdf_Dark else DocumentR.style.Theme_SS_Pdf_Light
        }
    }
}


@Composable
private fun rememberPdfConfiguration(
    context: Context,
    file: LocalFile,
    themeResId: Int, // Passed mapped theme based on LocalReaderStyle
    themeMode: ThemeMode // Used to handle actual PDF color inversion (Night Mode)
) = remember {
    PdfActivityConfiguration
        .Builder(context)
        .setUserInterfaceViewMode(UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_VISIBLE)
        .title(file.title)
        .themeMode(themeMode) // Maps to ThemeMode.DEFAULT or ThemeMode.NIGHT
        .theme(themeResId)    // Sets the custom Android UI theme for the UI and background
        .build()
}
