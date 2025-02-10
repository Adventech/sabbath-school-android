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

package ss.document.segment.components.video

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.widget.scaffold.HazeScaffold
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.BlockContent
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.primaryForeground
import kotlinx.collections.immutable.persistentListOf
import ss.document.components.DocumentTopAppBar
import ss.document.components.DocumentTopAppBarAction
import ss.document.segment.components.video.VideoSegmentScreen.Event
import ss.document.segment.components.video.VideoSegmentScreen.State
import ss.libraries.circuit.overlay.BottomSheetOverlay

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(VideoSegmentScreen::class, SingletonComponent::class)
@Composable
fun VideoSegmentScreenUi(state: State, modifier: Modifier = Modifier) {
    val readerStyle = LocalReaderStyle.current
    val containerColor = readerStyle.theme.background()
    val contentColor = readerStyle.theme.primaryForeground()
    val context = LocalContext.current

    HazeScaffold(
        modifier = modifier,
        topBar = {
            DocumentTopAppBar(
                title = {
                    Text(
                        text = state.title,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                collapsed = true,
                contentColor = contentColor,
                actions = persistentListOf(
                    DocumentTopAppBarAction.DisplayOptions,
                ),
                onNavBack = { state.eventSink(Event.OnNavBack) },
                onActionClick = { state.eventSink(Event.OnTopAppBarAction(it)) }
            )
        },
        blurTopBar = true,
        containerColor = containerColor,
        contentColor = contentColor,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(state.videos) { video ->
                VideoSegmentUi(
                    thumbnail = video.thumbnail,
                    title = video.title,
                    modifier = Modifier
                        .padding(horizontal = Dimens.grid_4)
                        .clickable { state.eventSink(Event.PlayVideo(context, video)) },
                )
            }

            item {
                Text(
                    text = state.title,
                    modifier = Modifier
                        .padding(horizontal = Dimens.grid_4),
                    style = Styler.textStyle(null).copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    ),
                    color = contentColor,
                )
            }

            items(state.blocks) { block ->
                BlockContent(
                    blockItem = block,
                    modifier = Modifier,
                    userInputState = state.userInputState,
                    onHandleUri = { _, _ -> },
                )
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
            }
        }
    }


    val overlayState = state.overlayState
    OverlayEffect(overlayState) {
        overlayState?.onResult(
            show(BottomSheetOverlay(
                skipPartiallyExpanded = overlayState.skipPartiallyExpanded,
            ) {
                ContentWithOverlays {
                    CircuitContent(screen = overlayState.screen)
                }
            })
        )
    }
}
