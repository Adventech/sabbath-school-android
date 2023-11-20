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

package app.ss.tv.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.ss.tv.presentation.home.HomeScreen.Event
import app.ss.tv.presentation.home.HomeScreen.State
import app.ss.tv.presentation.theme.ParentPadding
import com.slack.circuit.foundation.CircuitContent

@Composable
fun HomeScreenUi(state: State, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    var isTopBarVisible by remember(state) { mutableStateOf(state.topAppBarVisible) }
    val currentTopBarSelectedTabIndex by remember(state) { mutableIntStateOf(state.selectedIndex) }
    var isTopBarFocused by remember { mutableStateOf(false) }

    // 1. On user's first back press, bring focus to the current selected tab, if TopBar is not
    //    visible, first make it visible, then focus the selected tab
    // 2. On second back press, bring focus back to the first displayed tab
    // 3. On third back press, exit the app
    fun handleBackPress() {
        if (!isTopBarVisible) {
            isTopBarVisible = true
            TopBarFocusRequesters[currentTopBarSelectedTabIndex + 1].requestFocus()
        } else if (currentTopBarSelectedTabIndex == 0) {
            state.eventSink(Event.OnBack)
        } else if (!isTopBarFocused) {
            TopBarFocusRequesters[currentTopBarSelectedTabIndex + 1].requestFocus()
        } else {
            TopBarFocusRequesters[1].requestFocus()
        }
    }

    BackHandler(enabled = true) { handleBackPress() }

    Box(
        modifier = modifier.onPreviewKeyEvent {
            if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                handleBackPress()
                return@onPreviewKeyEvent true
            }
            false
        }
    ) {
        var wasTopBarFocusRequestedBefore by rememberSaveable { mutableStateOf(false) }
        var topBarHeightPx by rememberSaveable { mutableIntStateOf(0) }

        // Used to show/hide HomeTopBar
        val topBarYOffsetPx by animateIntAsState(
            targetValue = if (isTopBarVisible) 0 else -topBarHeightPx,
            animationSpec = tween(),
            label = "y-offset",
        )

        // Used to push down/pull up CircuitContent when the HomeTopBar is shown/hidden
        val contentTopPaddingDp by animateDpAsState(
            targetValue = if (isTopBarVisible) with(density) { topBarHeightPx.toDp() } else 0.dp,
            animationSpec = tween(),
            label = "top-padding",
        )

        LaunchedEffect(Unit) {
            if (!wasTopBarFocusRequestedBefore) {
                TopBarFocusRequesters[currentTopBarSelectedTabIndex + 1].requestFocus()
                wasTopBarFocusRequestedBefore = true
            }
        }

        HomeTopBar(
            modifier = Modifier
                .offset { IntOffset(x = 0, y = topBarYOffsetPx) }
                .onSizeChanged { topBarHeightPx = it.height }
                .onFocusChanged { isTopBarFocused = it.hasFocus }
                .padding(
                    horizontal = ParentPadding.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
                ),
            selectedTabIndex = currentTopBarSelectedTabIndex,
        ) { screen -> state.eventSink(Event.OnTopBarScreen(screen)) }

        CircuitContent(
            screen = state.currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentTopPaddingDp),
            onNavEvent = { state.eventSink(Event.OnNavEvent(it)) }
        )
    }
}
