/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import app.ss.design.compose.widget.scaffold.HazeScaffold
import app.ss.design.compose.widget.scaffold.SystemUiEffect
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.style.LocalBlocksStyle
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.LocalSegmentStyle
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import kotlinx.collections.immutable.persistentListOf
import ss.document.components.DocumentLoadingView
import ss.document.components.DocumentPager
import ss.document.components.DocumentTitleBar
import ss.document.components.DocumentTopAppBar
import ss.libraries.circuit.navigation.DocumentScreen
import ss.libraries.circuit.overlay.BottomSheetOverlay

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DocumentScreen::class, SingletonComponent::class)
@Composable
fun DocumentScreenUi(state: State, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var collapsed by remember { mutableStateOf(false) }
    val toolbarTitle by remember(state) { derivedStateOf { if (collapsed) state.title else "" } }
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

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DocumentTopAppBar(
                title = {
                    when (state) {
                        is State.Success -> {
                            DocumentTitleBar(
                                segments = state.segments,
                                selectedSegment = state.selectedSegment,
                                onSelection = { state.eventSink(SuccessEvent.OnSegmentSelection(it)) }
                            )
                        }

                        is State.Loading -> {
                            Text(toolbarTitle)
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                collapsible = state.hasCover,
                collapsed = collapsed,
                actions = (state as? State.Success)?.actions ?: persistentListOf(),
                onNavBack = { state.eventSink(Event.OnNavBack) },
                onActionClick = { state.eventSink(Event.OnActionClick(it)) }
            )
        },
        bottomBar = {

        },
        blurTopBar = !state.hasCover || collapsed,
    ) {
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
                        modifier = Modifier,
                        initialPage = state.initialPage,
                        onPageChange = { state.eventSink(SuccessEvent.OnPageChange(it)) },
                        onCollapseChange = { collapsed = it },
                        onNavEvent = { state.eventSink(SuccessEvent.OnNavEvent(it)) }
                    )
                }

                OverlayEffect(state.overlayState) {
                    when (val state = state.overlayState) {
                        is DocumentOverlayState.BottomSheet -> {
                            state.onResult(show(BottomSheetOverlay(state.skipPartiallyExpanded) { CircuitContent(state.screen) }))
                        }

                        null -> Unit
                    }
                }
            }
        }
    }

    SystemUiEffect(lightStatusBar)
}
