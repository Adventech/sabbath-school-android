/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.lessons.ui.quarterlies

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.extensions.previews.DevicePreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.design.compose.widget.scaffold.SsScaffold
import app.ss.models.QuarterlyGroup
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesGroupCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesListCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyList
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyListCallbacks
import androidx.compose.material.icons.Icons as MaterialIcons
import app.ss.translations.R.string as RString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuarterliesScreen(
    viewModel: QuarterliesViewModel = viewModel(),
    callbacks: QuarterlyListCallbacks
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    QuarterliesScreen(
        state = state.copy(
            title = viewModel.groupTitle ?: stringResource(id = RString.ss_app_name)
        ),
        callbacks = callbacks,
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuarterliesScreen(
    state: QuarterliesUiState,
    callbacks: QuarterlyListCallbacks,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    mainPadding: PaddingValues = PaddingValues(0.dp)
) {
    SsScaffold(
        topBar = {
            QuarterliesTopAppBar(
                title = state.title,
                spec = getTopAppBarSpec(type = callbacks),
                navigationIcon = {
                    NavIcon(photoUrl = state.photoUrl, type = callbacks)
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                )
            )
        },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->
        QuarterlyList(
            quarterlies = state.type,
            callbacks = callbacks,
            modifier = Modifier.padding(innerPadding),
            mainPadding = mainPadding
        )

        if (!state.isLoading) {
            val localView = LocalView.current
            LaunchedEffect(Unit) {
                // We're leveraging the fact, that the current view is directly set as content of Activity.
                val activity = localView.context as? Activity ?: return@LaunchedEffect
                // To be sure not to call in the middle of a frame draw.
                localView.doOnPreDraw { activity.reportFullyDrawn() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuarterliesTopAppBar(
    title: String,
    spec: TopAppBarSpec,
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {

    SsTopAppBar(
        spec = spec,
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = navigationIcon,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun getTopAppBarSpec(
    type: QuarterlyListCallbacks,
) = TopAppBarSpec(
    topAppBarType = TopAppBarType.Large,
    actions = when (type) {
        is QuarterliesGroupCallback -> listOf(
            IconButton(
                imageVector = MaterialIcons.Rounded.Translate,
                contentDescription = stringResource(id = RString.ss_quarterlies_filter_languages),
                onClick = { type.filterLanguages() }
            )
        )
        else -> emptyList()
    }
)

@Composable
private fun NavIcon(
    photoUrl: String?,
    type: QuarterlyListCallbacks,
) {
    when (type) {
        is QuarterliesGroupCallback -> {
            ContentBox(
                content = RemoteImage(
                    data = photoUrl,
                    contentDescription = stringResource(id = RString.ss_about),
                    loading = {
                        Spacer(
                            modifier = Modifier
                                .size(AccountImgSize)
                                .asPlaceholder(
                                    visible = true,
                                    shape = CircleShape
                                )
                        )
                    },
                    error = {
                        IconBox(
                            icon = Icons.AccountCircle,
                            modifier = Modifier
                                .size(AccountImgSize)
                                .clickable { type.profileClick() }
                        )
                    }
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(AccountImgSize)
                    .clip(CircleShape)
                    .clickable { type.profileClick() }
            )
        }
        is QuarterliesListCallback -> {
            IconButton(onClick = { type.backNavClick() }) {
                IconBox(Icons.ArrowBack)
            }
        }
    }
}

private val AccountImgSize = 32.dp

@OptIn(ExperimentalMaterial3Api::class)
@DevicePreviews
@Composable
private fun ScreenPreview() {
    SsTheme {
        QuarterliesScreen(
            state = QuarterliesUiState(
                title = "Sabbath School"
            ),
            callbacks = object : QuarterliesGroupCallback {
                override fun onSeeAllClick(group: QuarterlyGroup) {
                    // do nothing
                }

                override fun profileClick() {
                    // do nothing
                }

                override fun filterLanguages() {
                    // do nothing
                }

                override fun onReadClick(index: String) {
                    // do nothing
                }
            }
        )
    }
}
