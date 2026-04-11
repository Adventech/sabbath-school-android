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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.primaryForeground
import kotlinx.coroutines.launch
import ss.libraries.circuit.navigation.PdfScreen
import ss.libraries.circuit.overlay.BottomSheetOverlay

@CircuitInject(PdfScreen::class, SingletonComponent::class)
@Composable
fun ReadPdfUi(state: ReadPdfState, modifier: Modifier = Modifier) {
    when (state) {
        ReadPdfState.Loading -> Surface {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ReadPdfState.Success -> ReadPdfSuccessUi(state, modifier)
    }
}

@Composable
private fun ReadPdfSuccessUi(state: ReadPdfState.Success, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(
        pageCount = { state.documents.size },
    )
    val readerStyle = LocalReaderStyle.current
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalSsHapticFeedback.current
    val context = LocalContext.current

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        beyondViewportPageCount = 0,
    ) { page ->
        val document = state.documents[page]
        var expanded by remember { mutableStateOf(false) }

        PdfUi(
            document = document,
            topAppBarState = state.topAppBarState,
            config = state.config,
            modifier = Modifier,
            title = {
                val hasMultipleDocs = state.documents.size > 1
                Row(
                    modifier = if (hasMultipleDocs) {
                        Modifier
                            .sizeIn(minHeight = 48.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                expanded = true
                                hapticFeedback.performClick()
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    } else {
                        Modifier
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = document.file.title,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (hasMultipleDocs) {

                        IconBox(
                            icon = Icons.ArrowDropDown,
                            contentColor = readerStyle.theme.primaryForeground(),
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            state.documents.forEachIndexed { index, doc ->
                                DropdownMenuItem(
                                    text = { Text(doc.file.title) },
                                    onClick = {
                                        expanded = false
                                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            eventSink = state.eventSink,
        )
    }

    ReadPdfOverlay(state.overlayState) { state.eventSink(ReadPdfEvent.OnNavEvent(it, context)) }
}

@Composable
private fun ReadPdfOverlay(state: ReadPdfOverlayState, onNavEvent: (event: NavEvent) -> Unit,) {
    OverlayEffect(state::class.simpleName) {
        when (state) {
            is ReadPdfOverlayState.BottomSheet -> state.onResult(
                show(BottomSheetOverlay(
                    skipPartiallyExpanded = state.skipPartiallyExpanded,
                ) {
                    ContentWithOverlays {
                        CircuitContent(
                            screen = state.screen,
                            onNavEvent = onNavEvent,
                        )
                    }
                })
            )
            ReadPdfOverlayState.None -> Unit
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewLoading() {
    SsTheme { ReadPdfUi(ReadPdfState.Loading) }
}
