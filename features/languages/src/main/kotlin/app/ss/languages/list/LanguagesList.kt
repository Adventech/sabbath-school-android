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

package app.ss.languages.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.previews.DayNightPreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.divider.Divider
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.languages.state.LanguageModel
import app.ss.languages.state.ListState

@Composable
internal fun LanguagesList(
    state: ListState,
    onItemClick: (LanguageModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(state.models, key = { it.code }) { model ->

            @OptIn(ExperimentalFoundationApi::class)
            LanguageItem(
                model = model,
                modifier = Modifier
                    .clickable { onItemClick(model) }
                    .animateItemPlacement()
            )

            Divider()
        }
    }
}

@Composable
private fun LanguageItem(
    model: LanguageModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = model.nativeName,
                style = SsTheme.typography.labelMedium,
                color = SsTheme.colors.onSurfacePrimary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = model.name,
                style = SsTheme.typography.bodySmall,
                color = SsTheme.colors.onSurfaceSecondary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

        if (model.selected) {
            IconBox(icon = Icons.Check)
        }
    }
}

@DayNightPreviews
@Composable
private fun Preview() {
    PreviewItem(model = LanguageModel("en", "English", "English", false))
}

@DayNightPreviews
@Composable
private fun PreviewSelected() {
    PreviewItem(model = LanguageModel("es", "Spanish", "Español", true))
}

@Composable
private fun PreviewItem(model: LanguageModel) {
    SsTheme {
        Surface {
            LanguageItem(model = model)
        }
    }
}
