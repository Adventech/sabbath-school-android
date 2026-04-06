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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ss.design.compose.widget.scaffold.LocalNavbarController
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.UserInterfaceViewMode
import com.pspdfkit.configuration.theming.ThemeMode
import com.pspdfkit.jetpack.compose.interactors.DocumentState
import com.pspdfkit.jetpack.compose.interactors.getDefaultDocumentManager
import com.pspdfkit.jetpack.compose.interactors.rememberDocumentState
import com.pspdfkit.jetpack.compose.views.DocumentView
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.primaryForeground
import ss.libraries.pdf.api.LocalFile
import app.ss.translations.R as L10nR
import com.pspdfkit.R as PspdfR
import ss.document.R as DocumentR

@Composable
fun PdfUi(
    document: LocalFile,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = { Text(document.title) },
    eventSink: (ReadPdfEvent) -> Unit = {},
) {
    val context = LocalContext.current
    val documentUri = document.uri
    val readerStyle = LocalReaderStyle.current
    val themeResId = readerStyle.theme.toPdfThemeResId()

    // Resolve ThemeMode for PSPDFKit's built-in document inversion
    val themeMode = when (readerStyle.theme) {
        ReaderStyle.Theme.Dark -> ThemeMode.NIGHT
        ReaderStyle.Theme.Auto -> if (isSystemInDarkTheme()) ThemeMode.NIGHT else ThemeMode.DEFAULT
        else -> ThemeMode.DEFAULT
    }
    val pdfActivityConfiguration = rememberPdfConfiguration(context, document, themeResId, themeMode)

    val documentState = rememberDocumentState(documentUri, pdfActivityConfiguration)

    val bottomPadding by animateDpAsState(if (LocalNavbarController.current.enabled) 80.dp else 0.dp)

    Column(modifier = modifier.fillMaxSize()) {
        PdfTopAppBar(
            title = title,
            documentState = documentState,
            readerStyle = readerStyle,
            eventSink = eventSink,
        )

        DocumentView(
            documentState = documentState,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = bottomPadding),
            documentManager = getDefaultDocumentManager(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfTopAppBar(
    title: @Composable () -> Unit,
    documentState: DocumentState,
    readerStyle: ReaderStyleConfig,
    eventSink: (ReadPdfEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { eventSink(ReadPdfEvent.OnNavBack) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(L10nR.string.ss_action_back),
                )
            }
        },
        actions = {
            IconButton(onClick = {
                documentState.toggleView(PspdfR.id.pspdf__menu_option_edit_annotations)
            }) {
                Icon(
                    painter = painterResource(DocumentR.drawable.ic_pdf_annotations),
                    contentDescription = stringResource(L10nR.string.ss_annotations),
                )
            }
            IconButton(onClick = {
                documentState.toggleView(PspdfR.id.pspdf__menu_option_outline)
            }) {
                Icon(
                    painter = painterResource(DocumentR.drawable.ic_pdf_bookmark),
                    contentDescription = stringResource(PspdfR.string.pspdf__activity_menu_outline),
                )
            }
            IconButton(onClick = {
                documentState.toggleView(PspdfR.id.pspdf__menu_option_settings)
            }) {
                Icon(
                    painter = painterResource(DocumentR.drawable.ic_pdf_settings),
                    contentDescription = stringResource(PspdfR.string.pspdf__activity_menu_settings),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = readerStyle.theme.background(),
            navigationIconContentColor = readerStyle.theme.primaryForeground(),
            actionIconContentColor = readerStyle.theme.primaryForeground(),
            titleContentColor = readerStyle.theme.primaryForeground(),
        )
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
    themeResId: Int,
    themeMode: ThemeMode,
) = remember(themeResId, themeMode) {
    PdfActivityConfiguration
        .Builder(context)
        .setUserInterfaceViewMode(UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_VISIBLE)
        .defaultToolbarEnabled(false)
        .title(file.title)
        .themeMode(themeMode)
        .theme(themeResId)
        .build()
}
