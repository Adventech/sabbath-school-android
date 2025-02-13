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

package ss.document.segment.hidden

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.BlockContent
import io.adventech.blockkit.ui.style.LocalBlocksStyle
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.LocalSegmentStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import io.adventech.blockkit.ui.style.primaryForeground
import ss.document.DocumentOverlay
import ss.document.segment.components.SegmentHeader
import ss.document.segment.hidden.HiddenSegmentScreen.Event
import ss.document.segment.hidden.HiddenSegmentScreen.State

@CircuitInject(HiddenSegmentScreen::class, SingletonComponent::class)
@Composable
fun HiddenSegmentUi(state: State, modifier: Modifier = Modifier) {
    when (state) {
        is State.Loading -> HiddenSegmentContent(state, modifier)
        is State.Success -> HiddenSegmentContent(state, modifier)
    }
}

@Composable
private fun HiddenSegmentContent(state: State.Success, modifier: Modifier = Modifier) {
    val readerStyle = state.readerStyle
    val contentColor = readerStyle.theme.primaryForeground()

    CompositionLocalProvider(
        LocalFontFamilyProvider provides state.fontFamilyProvider,
        LocalBlocksStyle provides state.style?.blocks,
        LocalSegmentStyle provides state.style?.segment,
        LocalReaderStyle provides state.readerStyle,
    ) {
        HiddenSegmentScaffold(readerStyle, modifier) {
            item {
                SegmentHeader(
                    title = state.title,
                    subtitle = state.subtitle,
                    date = state.date,
                    contentColor = contentColor,
                    style = state.style?.segment ?: state.style?.segment,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = 0.dp, vertical = 8.dp
                    )
                )
            }
            items(state.blocks, key = { it.id }) { blockItem ->
                BlockContent(
                    blockItem = blockItem,
                    userInputState = state.userInputState,
                    onHandleUri = { uri, data ->
                        state.eventSink(Event.OnHandleUri(uri, data))
                    }
                )
            }
        }

        DocumentOverlay(state.overlayState, readerStyle) {
            state.eventSink(Event.OnNavEvent(it))
        }
    }
}

@Composable
private fun HiddenSegmentContent(state: State.Loading, modifier: Modifier = Modifier) {
    val readerStyle = state.readerStyle
    val contentColor = readerStyle.theme.primaryForeground()
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()

    HiddenSegmentScaffold(readerStyle, modifier) {
        itemsIndexed(listOf(1, 2, 3)) { index, item ->
            LoadingRectangle(
                contentColor,
                Modifier
                    .size(
                        width = (screenWidth * if (item == 2) 0.5f else 0.9f).dp,
                        height = 8.dp
                    )
                    .padding(horizontal = horizontalPadding)
            )
        }

        items(13) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            ) {
                LoadingRectangle(
                    contentColor,
                    Modifier.size(15.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    LoadingRectangle(
                        contentColor,
                        Modifier.size(
                            width = (screenWidth * 0.6f).dp,
                            height = 15.dp
                        )
                    )
                    LoadingRectangle(
                        contentColor,
                        Modifier.size(
                            width = (screenWidth * 0.3f).dp,
                            height = 10.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingRectangle(
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .asPlaceholder(
                true,
                color = contentColor.copy(alpha = 0.34f), shape = RoundedCornerShape(cornerRadius)
            )
    )
}

private val horizontalPadding = 16.dp
private val cornerRadius = 10.dp

@Composable
private fun HiddenSegmentScaffold(
    readerStyle: ReaderStyleConfig,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    val backgroundColor = readerStyle.theme.background()
    val contentColor = readerStyle.theme.primaryForeground()
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = backgroundColor,
        contentColor = contentColor,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection) + 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                end = contentPadding.calculateEndPadding(layoutDirection) + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
    }
}
