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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.design.compose.widget.scaffold.SsScaffold
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesGroupCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesListCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyList
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyListCallbacks
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.placeHolderQuarterlies

@Composable
fun QuarterliesScreen(
    viewModel: QuarterliesViewModel,
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

    SsScaffold(
        topBar = {
            QuarterliesTopAppBar(
                scrollBehavior = scrollBehavior,
                type = callbacks,
                title = viewModel.groupTitle ?: stringResource(id = R.string.ss_app_name),
                photoUrl = photoUrl,
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                )
            )
        },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->
        QuarterlyList(
            data = data,
            callbacks = callbacks,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun QuarterliesTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    type: QuarterlyListCallbacks,
    title: String,
    photoUrl: String?,
    modifier: Modifier = Modifier
) {
    val navigationIcon: @Composable () -> Unit
    val actions: List<IconButton>

    when (type) {
        is QuarterliesGroupCallback -> {
            navigationIcon = {
                ContentBox(
                    content = RemoteImage(
                        data = photoUrl,
                        errorRes = R.drawable.ic_account_circle,
                        contentDescription = stringResource(id = R.string.ss_about), // todo: Add strings
                        shape = CircleShape
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(32.dp)
                        .clickable { type.profileClick() }
                )
            }
            actions = listOf(
                IconButton(
                    imageVector = Icons.Rounded.Translate,
                    contentDescription = stringResource(id = R.string.ss_quarterlies_filter_languages),
                    onClick = { type.filterLanguages() }
                )
            )
        }
        is QuarterliesListCallback -> {
            navigationIcon = {
                IconBox(
                    icon = IconButton(
                        imageVector = Icons.Rounded.ArrowBack,
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
