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

package app.ss.quarterlies

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import app.ss.design.compose.widget.appbar.FeedTopAppBar
import app.ss.design.compose.widget.scaffold.HazeScaffold
import app.ss.quarterlies.components.QuarterlyList
import app.ss.quarterlies.overlay.AppBrandingOverlay
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.components.SingletonComponent
import ss.libraries.circuit.navigation.QuarterliesScreen
import ss.services.auth.overlay.AccountDialogOverlay
import app.ss.translations.R.string as L10nR

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(QuarterliesScreen::class, SingletonComponent::class)
@Composable
fun QuarterliesScreenUi(state: State, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FeedTopAppBar(
                photoUrl = state.photoUrl,
                title = stringResource(id = L10nR.ss_app_name),
                scrollBehavior = scrollBehavior,
                onNavigationClick = { state.eventSink(Event.ProfileClick) },
                onFilterLanguagesClick = { state.eventSink(Event.FilterLanguages) }
            )
        },
        blurTopBar = true,
    ) { innerPadding ->
        QuarterlyList(
            quarterlies = state.type,
            contentPadding = innerPadding,
            modifier = Modifier,
            onReadClick = { state.eventSink(Event.QuarterlySelected(it)) },
            onSeeAllClick = { state.eventSink(Event.SeeAll(it)) },
        )
    }

    state.overlayState?.run { OverlayContent(this) }
}

@Composable
private fun OverlayContent(state: OverlayState) {
    OverlayEffect(state) {
        when (state) {
            is OverlayState.AccountInfo -> state.onResult(show(AccountDialogOverlay(state.userInfo, state.showSettings)))
            is OverlayState.BrandingInfo -> {
                show(AppBrandingOverlay())
                state.onResult()
            }
        }
    }
}
