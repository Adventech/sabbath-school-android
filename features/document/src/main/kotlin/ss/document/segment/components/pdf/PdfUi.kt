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
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import app.ss.design.compose.widget.scaffold.LocalNavbarController
import app.ss.models.media.MediaAvailability
import com.pspdfkit.annotations.AnnotationType
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.ThumbnailBarMode
import com.pspdfkit.configuration.activity.UserInterfaceViewMode
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.settings.SettingsMenuItemType
import com.pspdfkit.configuration.sharing.ShareFeatures
import com.pspdfkit.jetpack.compose.interactors.DocumentListener
import com.pspdfkit.jetpack.compose.interactors.getDefaultDocumentManager
import com.pspdfkit.jetpack.compose.interactors.rememberDocumentState
import com.pspdfkit.jetpack.compose.views.DocumentView
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.primaryForeground
import ss.document.components.DocumentTopAppBarAction
import ss.libraries.pdf.api.LocalFile
import java.util.EnumSet
import app.ss.translations.R as L10nR
import com.pspdfkit.R as PspdfR
import ss.document.R as DocumentR

@Composable
fun PdfUi(
    document: LocalFile,
    mediaAvailability: MediaAvailability,
    config: PdfReaderConfig,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = { Text(document.title) },
    eventSink: (ReadPdfEvent) -> Unit = {},
) {
    val context = LocalContext.current
    val documentUri = document.uri
    val pdfActivityConfiguration = rememberPdfConfiguration(context, document, config)

    val documentState = rememberDocumentState(documentUri, pdfActivityConfiguration)

    val bottomPadding by animateDpAsState(if (LocalNavbarController.current.enabled) 120.dp else 0.dp)

    Column(modifier = modifier.fillMaxSize()) {
        PdfTopAppBar(
            title = title,
            state = PdfTopAppBarState(
                mediaAvailability = mediaAvailability,
                documentState = documentState,
            ),
            eventSink = eventSink,
        )

        val activity = LocalContext.current as? FragmentActivity

        DocumentView(
            documentState = documentState,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = bottomPadding)
                .onVisibilityChanged { visible ->
                    if (!visible) { // Save configuration once the document is not visible
                        val pdfFragment = activity?.supportFragmentManager?.findPdfFragment()
                        // todo: Also save annotations
                        pdfFragment?.let { eventSink(ReadPdfEvent.OnConfigurationChanged(it.configuration)) }
                    }
                },
            documentManager = getDefaultDocumentManager(
                documentListener = DocumentListener(onDocumentLoaded = {
                    // set annotations
                   // documentState.documentConnection.addAnnotationToPage()
                }),
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfTopAppBar(
    title: @Composable () -> Unit,
    state: PdfTopAppBarState,
    eventSink: (ReadPdfEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val readerTheme = LocalReaderStyle.current.theme
    val (mediaAvailability, documentState) = state

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
            if (mediaAvailability.audio) {
                val action = DocumentTopAppBarAction.Audio
                MediaIcon(
                    action = action,
                    onClick = { eventSink(ReadPdfEvent.OnTopAppBarAction(action)) },
                )
            }
            if (mediaAvailability.video) {
                val action = DocumentTopAppBarAction.Video
                MediaIcon(
                    action = action,
                    onClick = { eventSink(ReadPdfEvent.OnTopAppBarAction(action)) },
                )
            }
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
            containerColor = readerTheme.background(),
            navigationIconContentColor = readerTheme.primaryForeground(),
            actionIconContentColor = readerTheme.primaryForeground(),
            titleContentColor = readerTheme.primaryForeground(),
        )
    )
}

@Composable
private fun rememberPdfConfiguration(
    context: Context,
    file: LocalFile,
    config: PdfReaderConfig,
) = remember(file, config) {
    val excludedAnnotationTypes = ArrayList(EnumSet.allOf(AnnotationType::class.java))
    allowedAnnotations.forEach { excludedAnnotationTypes.remove(it) }

    PdfActivityConfiguration
        .Builder(context)
        .setUserInterfaceViewMode(UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_VISIBLE)
        .defaultToolbarEnabled(false)
        .title(file.title)
        .scrollMode(config.scrollMode)
        .layoutMode(config.layoutMode)
        .scrollDirection(config.scrollDirection)
        .themeMode(config.themeMode)
        .fitMode(PageFitMode.FIT_TO_WIDTH)
        .animateScrollOnEdgeTaps(true)
        .excludedAnnotationTypes(excludedAnnotationTypes)
        .setEnabledShareFeatures(EnumSet.noneOf(ShareFeatures::class.java))
        .setThumbnailBarMode(ThumbnailBarMode.THUMBNAIL_BAR_MODE_NONE)
        .setSettingsMenuItems(EnumSet.allOf(SettingsMenuItemType::class.java))
        .build()
}

private val allowedAnnotations = listOf(
    AnnotationType.HIGHLIGHT,
    AnnotationType.INK,
    AnnotationType.NOTE,
    AnnotationType.WATERMARK,
    AnnotationType.STRIKEOUT,
    AnnotationType.FREETEXT,
)

// I know :-(
private fun FragmentManager.findPdfFragment(): com.pspdfkit.ui.PdfFragment? {
    for (fragment in fragments) {
        if (fragment is com.pspdfkit.ui.PdfFragment) return fragment

        // Recursively search child fragments
        val child = fragment.childFragmentManager.findPdfFragment()
        if (child != null) return child
    }
    return null
}

@Composable
private fun MediaIcon(
    action: DocumentTopAppBarAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(action.iconRes),
            contentDescription = stringResource(action.title),
        )
    }
}
