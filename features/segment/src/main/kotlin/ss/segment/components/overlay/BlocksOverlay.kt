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

package ss.segment.components.overlay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.BlockContent
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.background
import kotlinx.collections.immutable.ImmutableList

class BlocksOverlay(private val state: State) : Overlay<BlocksOverlay.Result> {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        BasicAlertDialog(
            onDismissRequest = {
                navigator.finish(Result.Dismissed)
            },
            content = {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .clickable { navigator.finish(Result.Dismissed) }
                        .safeDrawingPadding()
                        .padding(horizontal = SsTheme.dimens.grid_4, vertical = SsTheme.dimens.grid_8),
                    shape = MaterialTheme.shapes.large,
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                ) {
                    DialogContent(
                        state = state,
                        onDismiss = { navigator.finish(Result.Dismissed) },
                    )
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        )
    }

    @Immutable
    data class State(
        val blocks: ImmutableList<BlockItem>
    )

    @Stable
    sealed interface Result {
        data object Dismissed : Result
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogContent(
    state: BlocksOverlay.State,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val readerStyle = LocalReaderStyle.current
    val backgroundColor = readerStyle.theme.background()

    Scaffold(
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ) {},
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        IconBox(Icons.Close)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                horizontal = SsTheme.dimens.grid_4
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            items(state.blocks, key = { it.id }) { blockItem ->
                BlockContent(
                    blockItem = blockItem,
                    onHandleUri = { uri, data ->
                        // Shouldn't expect overlay over an overlay
                    }
                )
            }

            item {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }
        }
    }
}
