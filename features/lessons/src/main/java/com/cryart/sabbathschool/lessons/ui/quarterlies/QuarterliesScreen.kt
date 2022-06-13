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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconSpec
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

    SsScaffold(
        topBar = {
            QuarterliesTopAppBar(
                scrollBehavior = scrollBehavior,
                type = callbacks,
                title = viewModel.groupTitle ?: stringResource(id = R.string.ss_app_name)
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
    title: String
) {
    val navIcon: IconSpec
    val actions: List<IconSpec>

    when (type) {
        is QuarterliesGroupCallback -> {
            navIcon = IconSpec(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = stringResource(id = R.string.ss_about), // todo: Add strings
                onClick = { type.profileClick() }
            )
            actions = listOf(
                IconSpec(
                    imageVector = Icons.Rounded.Translate,
                    contentDescription = stringResource(id = R.string.ss_quarterlies_filter_languages),
                    onClick = { type.filterLanguages() }
                )
            )
        }
        is QuarterliesListCallback -> {
            navIcon = IconSpec(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(id = R.string.blank), // todo: Add strings
                onClick = { type.backNavClick() }
            )
            actions = emptyList()
        }
        else -> return
    }

    SsTopAppBar(
        spec = TopAppBarSpec(
            title = title,
            topAppBarType = TopAppBarType.Large,
            navIconSpec = navIcon,
            actions = actions
        ),
        scrollBehavior = scrollBehavior
    )
}
