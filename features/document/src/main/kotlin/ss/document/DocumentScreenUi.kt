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

package ss.document

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.scaffold.HazeScaffold
import app.ss.design.compose.widget.scaffold.SystemUiEffect
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.components.SingletonComponent
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import io.adventech.blockkit.model.resource.SegmentType
import io.adventech.blockkit.ui.style.LocalBlocksStyle
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.LocalSegmentStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import io.adventech.blockkit.ui.style.primaryForeground
import kotlinx.collections.immutable.persistentListOf
import ss.document.components.DocumentLoadingView
import ss.document.components.DocumentPager
import ss.document.components.DocumentTitleBar
import ss.document.components.DocumentTopAppBar
import ss.document.segment.components.overlay.BlocksOverlay
import ss.document.segment.components.overlay.ExcerptOverlay
import ss.libraries.circuit.navigation.DocumentScreen
import ss.libraries.circuit.navigation.MiniAudioPlayerScreen
import ss.libraries.circuit.overlay.BottomSheetOverlay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@CircuitInject(DocumentScreen::class, SingletonComponent::class)
@Composable
fun DocumentScreenUi(state: State, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val hapticFeedback = LocalSsHapticFeedback.current
    val context = LocalContext.current

    val density = LocalDensity.current
    val topPadding = WindowInsets.safeContent.asPaddingValues().calculateTopPadding()
    var collapsed by remember { mutableStateOf(false) }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val lightStatusBar by remember(isSystemInDarkTheme, state.hasCover, collapsed) {
        derivedStateOf {
            when {
                isSystemInDarkTheme -> false
                state.hasCover -> collapsed
                else -> true // Check reader theme here
            }
        }
    }

    val containerColor = state.containerColor()
    val contentColor = state.contentColor()

    HazeScaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .keepScreenOn(),
        topBar = {
            AnimatedVisibility(
                visible = state.showTopBar(collapsed),
                enter = slideInVertically {
                    with(density) { -(topPadding + 40.dp).roundToPx() }
                } + expandVertically(
                    expandFrom = Alignment.Top
                ) + fadeIn(
                    initialAlpha = 0.3f
                ),
                exit = fadeOut()
            ) {
                DocumentTopAppBar(
                    title = {
                        (state as? State.Success)?.let {
                            DocumentTitleBar(
                                segments = state.segments,
                                selectedSegment = state.selectedSegment,
                                contentColor = contentColor,
                                onSelection = { state.eventSink(SuccessEvent.OnSegmentSelection(it)) }
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    collapsible = state.hasCover,
                    collapsed = collapsed,
                    contentColor = contentColor,
                    actions = (state as? State.Success)?.actions ?: persistentListOf(),
                    onNavBack = {
                        hapticFeedback.performClick()
                        state.eventSink(Event.OnNavBack)
                    },
                    onActionClick = {
                        hapticFeedback.performClick()
                        state.eventSink(Event.OnActionClick(it, context))
                    }
                )
            }
        },
        containerColor = containerColor,
        contentColor = contentColor,
        bottomBar = {
            val hidePlayer = (state as? State.Success)?.selectedSegment?.type == SegmentType.STORY && !collapsed
            if (!hidePlayer) {
                CircuitContent(
                    screen = MiniAudioPlayerScreen,
                    onNavEvent = {
                        state.eventSink(SuccessEvent.OnNavEvent(it, context))
                    }
                )
            }
        },
        hazeStyle = HazeMaterials.regular(containerColor),
        blurTopBar = !state.hasCover || collapsed,
    ) { contentPadding ->
        when (state) {
            is State.Loading -> {
                DocumentLoadingView()
            }

            is State.Success -> {
                CompositionLocalProvider(
                    LocalFontFamilyProvider provides state.fontFamilyProvider,
                    LocalBlocksStyle provides state.style?.blocks,
                    LocalSegmentStyle provides state.style?.segment,
                    LocalReaderStyle provides state.readerStyle,
                ) {
                    DocumentPager(
                        segments = state.segments,
                        titleBelowCover = state.titleBelowCover,
                        documentId = state.documentId,
                        documentIndex = state.documentIndex,
                        resourceIndex = state.resourceIndex,
                        userInputState = state.userInputState,
                        modifier = Modifier.fillMaxSize(),
                        initialPage = state.initialPage,
                        onPageChange = { state.eventSink(SuccessEvent.OnPageChange(it)) },
                        onNavBack = { state.eventSink(Event.OnNavBack) },
                        onCollapseChange = { collapsed = it },
                        onHandleUri = { uri, blocks -> state.eventSink(SuccessEvent.OnHandleUri(uri, blocks)) },
                        onHandleReference = { state.eventSink(SuccessEvent.OnHandleReference(it)) },
                        onNavEvent = { state.eventSink(SuccessEvent.OnNavEvent(it, context)) },
                    )
                }

                DocumentOverlay(state.overlayState, state.readerStyle) {
                    state.eventSink(SuccessEvent.OnNavEvent(it, context))
                }
            }
        }
    }

    SystemUiEffect(lightStatusBar)
}


@Composable
internal fun DocumentOverlay(
    documentOverlayState: DocumentOverlayState?,
    readerStyle: ReaderStyleConfig,
    onNavEvent: (event: NavEvent) -> Unit,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    val containerColor = readerStyle.theme.background()
    val contentColor = readerStyle.theme.primaryForeground()

    OverlayEffect(documentOverlayState?.let { it::class.simpleName } ?: Unit) {
        when (val overlayState = documentOverlayState) {
            is DocumentOverlayState.BottomSheet -> {

                overlayState.onResult(
                    show(BottomSheetOverlay(
                        skipPartiallyExpanded = overlayState.skipPartiallyExpanded,
                        containerColor = containerColor.takeIf { overlayState.themed },
                        contentColor = contentColor.takeIf { overlayState.themed },
                    ) {
                        ContentWithOverlays {
                            CircuitContent(
                                screen = overlayState.screen,
                                onNavEvent = onNavEvent,
                            )
                        }

                        LaunchedEffect(Unit) {
                            // Feedback already provided on the top bar
                            if (overlayState.feedback) {
                                hapticFeedback.performScreenView()
                            }
                        }
                    })
                )
            }

            is DocumentOverlayState.Segment.Blocks -> overlayState.onResult(
                show(BlocksOverlay(overlayState.state))
            )

            is DocumentOverlayState.Segment.Excerpt -> overlayState.onResult(
                show(ExcerptOverlay(overlayState.state))
            )

            is DocumentOverlayState.Segment.None -> Unit
            null -> Unit
        }
    }
}

private fun State.showTopBar(collapsed: Boolean): Boolean = when (this) {
    is State.Loading -> true
    is State.Success -> when (selectedSegment?.type) {
        SegmentType.VIDEO -> false
        SegmentType.STORY -> collapsed
        SegmentType.PDF,
        SegmentType.UNKNOWN,
        SegmentType.BLOCK,

        null -> true
    }
}

@Composable
private fun State.containerColor(): Color = when (this) {
    is State.Loading -> SsTheme.colors.primaryBackground
    is State.Success -> readerStyle.theme.background()
}

@Composable
private fun State.contentColor(): Color = when (this) {
    is State.Loading -> SsTheme.colors.primaryForeground
    is State.Success -> readerStyle.theme.primaryForeground()
}
