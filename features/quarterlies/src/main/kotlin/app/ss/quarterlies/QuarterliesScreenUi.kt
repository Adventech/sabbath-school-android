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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import app.ss.quarterlies.components.QuarterlyList
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import ss.libraries.circuit.navigation.QuarterliesScreen
import androidx.compose.material.icons.Icons as MaterialIcons
import app.ss.translations.R.string as L10nR

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(QuarterliesScreen::class, SingletonComponent::class)
@Composable
fun QuarterliesScreenUi(state: State, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    SsScaffold(
        topBar = {
            SsTopAppBar(
                spec = getTopAppBarSpec { state.eventSink(Event.FilterLanguages) },
                modifier = modifier,
                title = { Text(text = stringResource(id = L10nR.ss_app_name)) },
                navigationIcon = {
                    NavIcon(photoUrl = state.photoUrl) {
                        state.eventSink(Event.ProfileClick)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->
        QuarterlyList(
            quarterlies = state.type,
            modifier = Modifier.padding(innerPadding),
            onReadClick = { state.eventSink(Event.QuarterlySelected(it)) },
            onSeeAllClick = { state.eventSink(Event.SeeAll(it)) },
        )
    }
}


@Composable
private fun getTopAppBarSpec(
    onClick: () -> Unit
) = TopAppBarSpec(
    topAppBarType = TopAppBarType.Large,
    actions = listOf(
        IconButton(
            imageVector = MaterialIcons.Rounded.Translate,
            contentDescription = stringResource(id = L10nR.ss_quarterlies_filter_languages),
            onClick = onClick
        )
    )
)

@Composable
private fun NavIcon(
    photoUrl: String?,
    onClick: () -> Unit
) {
    ContentBox(
        content = RemoteImage(
            data = photoUrl,
            contentDescription = stringResource(id = L10nR.ss_about),
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
                        .clickable { onClick() }
                )
            }
        ),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .size(AccountImgSize)
            .clip(CircleShape)
            .clickable { onClick() }
    )
}

private val AccountImgSize = 32.dp
