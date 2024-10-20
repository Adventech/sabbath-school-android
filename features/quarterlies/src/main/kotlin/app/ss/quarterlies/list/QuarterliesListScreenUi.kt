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

package app.ss.quarterlies.list

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.scaffold.SsScaffold
import app.ss.quarterlies.components.QuarterlyList
import app.ss.quarterlies.list.QuarterliesListScreen.Event
import app.ss.quarterlies.list.QuarterliesListScreen.State
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(QuarterliesListScreen::class, SingletonComponent::class)
@Composable
fun QuarterliesListScreenUi(state: State, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    SsScaffold(
        topBar = {
            SsTopAppBar(
                spec = TopAppBarSpec(TopAppBarType.Large),
                modifier = modifier,
                title = { Text(text = state.title) },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(Event.OnNavBack) }) {
                        IconBox(Icons.ArrowBack)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        scrollBehavior = scrollBehavior
    ) { paddingValues ->
        QuarterlyList(
            quarterlies = state.type,
            contentPadding = paddingValues,
            modifier = Modifier,
            onReadClick = { state.eventSink(Event.QuarterlySelected(it)) },
        )
    }
}
