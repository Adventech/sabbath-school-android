/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.tv.presentation.account

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.DenseListItem
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.ss.tv.presentation.account.AccountScreen.Event
import app.ss.tv.presentation.account.AccountScreen.State
import app.ss.tv.presentation.theme.SSTvTheme
import app.ss.tv.presentation.theme.rememberChildPadding
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccountUiScreen(state: State, modifier: Modifier = Modifier) {
    val childPadding = rememberChildPadding()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isLeftColumnFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = childPadding.start, vertical = childPadding.top)
    ) {
        TvLazyColumn(
            modifier = Modifier
                .fillMaxWidth(fraction = sidebarWidthFraction)
                .fillMaxHeight()
                .onFocusChanged { isLeftColumnFocused = it.hasFocus }
                .focusRestorer()
                .focusGroup()
        ) {
            itemsIndexed(AccountScreens.values(), key = { _, screen -> screen.name }) { index, screen ->
                DenseListItem(
                    selected = state.accountScreen == screen,
                    onClick = {
                        state.eventSink(Event.OnNav(screen))
                        focusManager.moveFocus(FocusDirection.Right)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (index == 0) Modifier.focusRequester(focusRequester)
                            else Modifier
                        )
                        .onFocusChanged {
                            if (it.isFocused && state.accountScreen != screen) {
                                state.eventSink(Event.OnNav(screen))
                            }
                        },
                    trailingContent = {
                        Icon(
                            screen.icon,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .padding(start = 4.dp)
                                .size(20.dp),
                            contentDescription = stringResource(screen.title)
                        )
                    }
                ) {
                    Text(
                        text = stringResource(screen.title),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        CircuitContent(
            screen = state.accountScreen.circuitScreen,
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent {
                    if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                        // Using 'while' because AccountsScreen has a grid that has multiple items
                        // in a row for which we would need to press D-Pad Left multiple times
                        while (!isLeftColumnFocused) {
                            focusManager.moveFocus(FocusDirection.Left)
                        }
                        return@onPreviewKeyEvent true
                    }
                    false
                },
        )
    }
}

private const val sidebarWidthFraction: Float = 0.32f

@Preview(device = Devices.TV_1080p)
@Composable
private fun Preview() {
    SSTvTheme {
        CircuitCompositionLocals(circuit = Circuit.Builder().build()) {
            AccountUiScreen(State(AccountScreens.About) {})
        }
    }
}
