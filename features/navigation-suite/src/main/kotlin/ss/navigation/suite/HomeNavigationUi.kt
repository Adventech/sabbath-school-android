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


package ss.navigation.suite

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.ss.design.compose.widget.scaffold.HazeScaffold
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.CircuitContent
import dagger.hilt.components.SingletonComponent
import ss.libraries.circuit.navigation.HomeNavScreen

@CircuitInject(HomeNavScreen::class, SingletonComponent::class)
@Composable
fun HomeNavigationUi(state: State, modifier: Modifier = Modifier) {
    when (state) {
        is State.Loading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is State.Fallback -> CircuitContent(
            screen = state.selectedItem,
            modifier = modifier.fillMaxSize(),
            onNavEvent = { state.eventSink(State.Fallback.Event.OnNavEvent(it)) },
        )

        is State.NavbarNavigation -> NavigationSuite(
            state = state,
            modifier = modifier,
        )
    }
}

@Composable
private fun NavigationSuite(
    state: State.NavbarNavigation,
    modifier: Modifier = Modifier,
) {
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())

    val content: @Composable (PaddingValues) -> Unit = {
        AnimatedContent(
            targetState = state.selectedItem,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)).togetherWith(fadeOut(animationSpec = tween(300)))
            },
            label = "content",
        ) { screen ->
            CircuitContent(
                screen = screen,
                modifier = Modifier.fillMaxSize(),
                onNavEvent = { state.eventSink(State.NavbarNavigation.Event.OnNavEvent(it)) },
            )
        }
    }

    when (layoutType) {
        NavigationSuiteType.NavigationBar -> {
            // Only apply HazeScaffold if the layout is NavigationBar
            HazeScaffold(
                modifier = modifier,
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier,
                        containerColor = Color.Transparent,
                    ) {
                        state.items.forEach { model ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(model.iconRes),
                                        contentDescription = stringResource(model.title),
                                    )
                                },
                                selected = model.screen() == state.selectedItem,
                                onClick = { state.eventSink(State.NavbarNavigation.Event.OnItemSelected(model)) },
                            )
                        }
                    }
                },
                blurBottomBar = true,
                content = content,
            )
        }
        else -> {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    state.items.forEach { model ->
                        item(
                            icon = {
                                Icon(
                                    painter = painterResource(model.iconRes),
                                    contentDescription = stringResource(model.title),
                                )
                            },
                            selected = state.selectedItem == model.screen(),
                            onClick = { state.eventSink(State.NavbarNavigation.Event.OnItemSelected(model)) },
                        )
                    }
                },
                modifier = modifier,
            ) {
                content(PaddingValues())
            }
        }
    }
}
