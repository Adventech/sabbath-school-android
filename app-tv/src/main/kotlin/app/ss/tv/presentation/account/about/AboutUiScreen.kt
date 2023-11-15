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

package app.ss.tv.presentation.account.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.ss.tv.R
import app.ss.tv.presentation.account.about.AboutScreen.State
import app.ss.tv.presentation.theme.SSTvTheme
import app.ss.tv.presentation.theme.rememberChildPadding
import app.ss.translations.R as L10nR

@Composable
fun AboutUiScreen(state: State, modifier: Modifier = Modifier) {

    val childPadding = rememberChildPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = childPadding.start)
            .verticalScroll(rememberScrollState())
            .focusable(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(L10nR.string.ss_about),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall
        )

        Image(
            painter = painterResource(R.drawable.gc_sspm_logo),
            contentDescription = stringResource(L10nR.string.gc_sspm_logo_content_description),
            modifier = Modifier.size(240.dp, 120.dp),
            colorFilter = ColorFilter.tint(LocalContentColor.current)
        )

        Text(
            text = stringResource(id = L10nR.string.ss_about_made),
            style = MaterialTheme.typography.bodyMedium
        )

        Divider(Modifier.padding(vertical = 24.dp))

        Text(
            text = stringResource(L10nR.string.ss_settings_version_with_param_string, state.version),
            modifier = Modifier.graphicsLayer { alpha = 0.6f },
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun Preview() {
    SSTvTheme { AboutUiScreen(State("1.0.0")) }
}
