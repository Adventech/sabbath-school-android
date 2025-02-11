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

package ss.document.segment.components.overlay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.ExcerptItemContent
import io.adventech.blockkit.ui.ExcerptOptions
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.primaryForeground
import kotlinx.coroutines.launch

class ExcerptOverlay(private val state: State) : Overlay<ExcerptOverlay.Result> {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        BlocksDialogSurface(
            readerStyle = state.style,
            modifier = Modifier,
            onDismiss = { navigator.finish(Result.Dismissed) }
        ) {
            DialogContent(
                state = state,
                onDismiss = { navigator.finish(Result.Dismissed) },
            )
        }
    }

    @Immutable
    data class State(
        val excerpt: BlockItem.Excerpt,
        val style: ReaderStyleConfig,
        val userInputState: UserInputState,
    )

    @Stable
    sealed interface Result {
        data object Dismissed : Result
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogContent(
    state: ExcerptOverlay.State,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val readerStyle = LocalReaderStyle.current
    val backgroundColor = readerStyle.theme.background()
    val contentColor = readerStyle.theme.primaryForeground()
    val blockItem = state.excerpt

    var selectedOption by remember { mutableStateOf(blockItem.options.firstOrNull()) }
    val selectedItem = remember(selectedOption) { blockItem.items.firstOrNull { it.option == selectedOption } }
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopEnd),
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier,
            offset = DpOffset(-(16).dp, 0.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            ExcerptOptions(
                options = blockItem.options,
                selectedOption = selectedOption,
                onOptionSelected = { option ->
                    expanded = false
                    selectedOption = option
                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                }
            )
        }
    }


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
                        IconBox(Icons.Close, contentColor = contentColor)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .sizeIn(minHeight = 48.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = selectedOption ?: "",
                            modifier = Modifier,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = Styler.defaultFontFamily()
                            ),
                            color = contentColor
                        )

                        IconBox(icon = Icons.ArrowDropDown, contentColor = contentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor,
        contentColor = contentColor,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SsTheme.dimens.grid_4)
                .verticalScroll(scrollState)
        ) {
            selectedItem?.let {
                ExcerptItemContent(
                    blockItem = it,
                    modifier = Modifier,
                    userInputState = state.userInputState,
                )
            }

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}
