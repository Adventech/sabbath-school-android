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

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.widget.list.LocalScrollToTop
import app.ss.design.compose.widget.scaffold.HazeScaffold
import app.ss.design.compose.widget.scaffold.LocalNavbarController
import app.ss.design.compose.widget.scaffold.NavbarController
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.navigation.intercepting.AndroidScreenAwareNavigationInterceptor
import com.slack.circuitx.navigation.intercepting.rememberInterceptingNavigator
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.LoginScreen
import ss.navigation.suite.State.NavbarNavigation.Event as NavbarEvent

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
    val hapticFeedback = LocalSsHapticFeedback.current
    val activity = requireNotNull(LocalActivity.current) { "Local activity not provided" }
    val context = LocalContext.current
    var showBottomBar by remember { mutableStateOf(true) }
    val controller = remember {
        object: NavbarController {
            override fun hide() { showBottomBar = false }
            override fun show() { showBottomBar = true }
        }
    }
    val scrollToTopSignal = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

    val interceptors = remember(context, activity) {
        persistentListOf(
            AndroidScreenAwareNavigationInterceptor(context),
            state.supportInterceptorFactory.create(activity),
            RootScreenInterceptor(
                onReset = { screen ->
                    state.eventSink(NavbarEvent.OnReset(screen))
                    if (screen is LoginScreen) {
                        showBottomBar = false
                    }
                }
            )
        )
    }

    val backStack = rememberTabBackStack(
        items = state.items,
        selectedScreen = state.selectedItem,
        rootScreenProvider = { it.screen() },
    )

    val content: @Composable (PaddingValues) -> Unit = {
        val circuitNavigator = rememberCircuitNavigator(backStack)
        val navigator = rememberInterceptingNavigator(
            navigator = circuitNavigator,
            interceptors = interceptors,
        )

        CompositionLocalProvider(
            LocalNavbarController provides controller,
            LocalScrollToTop provides scrollToTopSignal,
        ) {
            NavigableCircuitContent(
                navigator = navigator,
                backStack = backStack,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    fun handleSelection(isSelected: Boolean, model: NavbarItem) {
        if (isSelected) {
            if (backStack.size > 1) {
                // Reset backstack to the home screen i.e
                // clear every screen in this stack
                backStack.popUntil { it.screen == state.selectedItem }
            } else {
                // Signal a scroll-to-top on current screen
                scrollToTopSignal.tryEmit(Unit)
            }
        } else {
            state.eventSink(NavbarEvent.OnItemSelected(model))
            hapticFeedback.performSegmentSwitch()
        }
    }

    when (layoutType) {
        NavigationSuiteType.NavigationBar -> {
            // Only apply HazeScaffold if the layout is NavigationBar
            HazeScaffold(
                modifier = modifier,
                bottomBar = {
                    AnimatedVisibility(
                        visible = showBottomBar,
                        modifier = Modifier.fillMaxWidth(),
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 150,
                                delayMillis = 50
                            )
                        ) + slideInVertically { it },
                    ) {
                        NavigationBar(
                            modifier = Modifier,
                            containerColor = Color.Transparent,
                        ) {
                            state.items.forEach { model ->
                                val isSelected = model.screen() == state.selectedItem
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            painter = painterResource(model.iconRes),
                                            contentDescription = stringResource(model.title),
                                        )
                                    },
                                    selected = isSelected,
                                    onClick = { handleSelection(isSelected, model) },
                                )
                            }
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
                        val isSelected = state.selectedItem == model.screen()
                        item(
                            icon = {
                                Icon(
                                    painter = painterResource(model.iconRes),
                                    contentDescription = stringResource(model.title),
                                )
                            },
                            selected = isSelected,
                            onClick = { handleSelection(isSelected, model) },
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

@Composable
private fun <T> rememberTabBackStack(
    items: List<T>,
    selectedScreen: Screen,
    rootScreenProvider: (T) -> Screen,
): SaveableBackStack {
    // We iterate over all defined tabs to ensure a BackStack is
    // consistently provided (remembered) for each one.
    val backStacks = items.associate { item ->
        val root = rootScreenProvider(item)

        // key(root) ensures that this specific rememberSaveableBackStack
        // is tied to this specific tab, even if the list order changes.
        root to key(root) {
            rememberSaveableBackStack(root = root)
        }
    }

    return backStacks[selectedScreen]
        ?: error("Selected screen $selectedScreen is not found in the provided tab items.")
}
