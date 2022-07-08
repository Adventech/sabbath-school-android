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

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.cryart.sabbathschool.lessons.ui.quarterlies

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.design.compose.extensions.flow.rememberFlowWithLifecycle
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.design.compose.widget.scaffold.SsScaffold
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesGroupCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesListCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyList
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyListCallbacks
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.placeHolderQuarterlies
import androidx.compose.material.icons.Icons as MaterialIcons

@Composable
internal fun QuarterliesScreen(
    viewModel: QuarterliesViewModel = viewModel(),
    callbacks: QuarterlyListCallbacks
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberSplineBasedDecay(),
        rememberTopAppBarScrollState()
    )
    val data by rememberFlowWithLifecycle(viewModel.quarterliesFlow)
        .collectAsState(initial = GroupedQuarterlies.TypeList(placeHolderQuarterlies()))

    val photoUrl by rememberFlowWithLifecycle(viewModel.photoUrlFlow)
        .collectAsState(initial = null)

    QuarterliesScreen(
        quarterlies = data,
        title = viewModel.groupTitle ?: stringResource(id = R.string.ss_app_name),
        photoUrl = photoUrl,
        callbacks = callbacks,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
internal fun QuarterliesScreen(
    quarterlies: GroupedQuarterlies,
    title: String,
    callbacks: QuarterlyListCallbacks,
    photoUrl: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    SsScaffold(
        topBar = {
            QuarterliesTopAppBar(
                scrollBehavior = scrollBehavior,
                type = callbacks,
                title = title,
                photoUrl = photoUrl,
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                )
            )
        },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->
        QuarterlyList(
            data = quarterlies,
            callbacks = callbacks,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
internal fun QuarterliesTopAppBar(
    title: String,
    type: QuarterlyListCallbacks,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    photoUrl: String? = null
) {
    val navigationIcon: @Composable () -> Unit
    val actions: List<IconButton>

    when (type) {
        is QuarterliesGroupCallback -> {
            navigationIcon = {
                ContentBox(
                    content = RemoteImage(
                        data = photoUrl,
                        contentDescription = stringResource(id = R.string.ss_about),
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
            actions = listOf(
                IconButton(
                    imageVector = MaterialIcons.Rounded.Translate,
                    contentDescription = stringResource(id = R.string.ss_quarterlies_filter_languages),
                    onClick = { type.filterLanguages() }
                )
            )
        }
        is QuarterliesListCallback -> {
            navigationIcon = {
                IconBox(
                    icon = IconButton(
                        imageVector = MaterialIcons.Rounded.ArrowBack,
                        contentDescription = "Back", // todo: Add strings
                        onClick = { type.backNavClick() },
                    )
                )
            }
            actions = emptyList()
        }
        else -> return
    }

    SsTopAppBar(
        spec = TopAppBarSpec(
            topAppBarType = TopAppBarType.Large,
            actions = actions
        ),
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = navigationIcon,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

private val AccountImgSize = 32.dp
