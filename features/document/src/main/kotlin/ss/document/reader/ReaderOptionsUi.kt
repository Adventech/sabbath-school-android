/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.document.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TextFormat
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.divider.Divider
import app.ss.design.compose.widget.material.LegacySlider
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.style.ReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.Styler
import ss.document.reader.ReaderOptionsScreen.Event
import ss.document.reader.ReaderOptionsScreen.State
import app.ss.translations.R as L10nR

@CircuitInject(ReaderOptionsScreen::class, SingletonComponent::class)
@Composable
fun ReaderOptionsUi(state: State, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ReaderOptionsTheme(state.config.theme) { state.eventSink(Event.OnThemeChanged(it)) }

        Divider(Modifier.padding(vertical = 12.dp))

        ReaderOptionsTypeface(state.config.typeface) { state.eventSink(Event.OnTypefaceChanged(it)) }

        Divider(Modifier.padding(vertical = 12.dp))

        ReaderOptionsFontSize(state.config.size) { state.eventSink(Event.OnFontSizeChanged(it)) }

        Spacer(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
        )
    }
}

@Composable
private fun ReaderOptionsTheme(
    theme: ReaderStyle.Theme,
    modifier: Modifier = Modifier,
    onSelected: (ReaderStyle.Theme) -> Unit = {},
) {
    Text(
        text = stringResource(L10nR.string.ss_settings_theme),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = SsTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(ReaderStyle.Theme.entries) { item ->
            FilterChip(
                selected = theme == item,
                onClick = { onSelected(item) },
                label = {
                    Text(
                        text = stringResource(item.label()),
                        style = SsTheme.typography.titleMedium
                    )
                },
                modifier = Modifier,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

private fun ReaderStyle.Theme.label() = when (this) {
    ReaderStyle.Theme.Light -> L10nR.string.ss_reading_display_options_theme_light
    ReaderStyle.Theme.Sepia -> L10nR.string.ss_reading_display_options_theme_sepia
    ReaderStyle.Theme.Dark -> L10nR.string.ss_reading_display_options_theme_dark
    ReaderStyle.Theme.Auto -> L10nR.string.ss_reading_display_options_theme_default
}

@Composable
private fun ReaderOptionsTypeface(
    typeface: ReaderStyle.Typeface,
    modifier: Modifier = Modifier,
    onSelected: (ReaderStyle.Typeface) -> Unit = {}
) {
    Text(
        text = stringResource(L10nR.string.ss_settings_typeface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = SsTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(ReaderStyle.Typeface.entries) { item ->
            FilterChip(
                selected = typeface == item,
                onClick = { onSelected(item) },
                label = {
                    Text(
                        text = stringResource(item.label()),
                        style = SsTheme.typography.titleMedium.copy(
                            fontFamily = Styler.fontFamily(item)
                        )
                    )
                },
                modifier = Modifier,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

private fun ReaderStyle.Typeface.label() = when (this) {
    ReaderStyle.Typeface.Lato -> L10nR.string.ss_reading_display_options_typeface_lato
    ReaderStyle.Typeface.Andada -> L10nR.string.ss_reading_display_options_typeface_andada
    ReaderStyle.Typeface.PtSans -> L10nR.string.ss_reading_display_options_typeface_pt_sans
    ReaderStyle.Typeface.PtSerif -> L10nR.string.ss_reading_display_options_typeface_pt_serif
}

@Composable
private fun ReaderOptionsFontSize(
    size: ReaderStyle.Size,
    modifier: Modifier = Modifier,
    onSelected: (ReaderStyle.Size) -> Unit = {}
) {
    var sliderPosition by remember(size) { mutableFloatStateOf(size.step()) }

    Text(
        text = stringResource(L10nR.string.ss_settings_font_size),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = SsTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Icon(
            imageVector = Icons.Rounded.TextFormat,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = SsTheme.colors.primary
        )

        LegacySlider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                it.toSize()?.let { onSelected(it) }
            },
            modifier = Modifier.weight(1f),
            steps = 3,
            valueRange = 0f..4f
        )

        Icon(
            imageVector = Icons.Rounded.TextFormat,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = SsTheme.colors.primary
        )
    }
}

private fun ReaderStyle.Size.step() = when (this) {
    ReaderStyle.Size.Tiny -> 0f
    ReaderStyle.Size.Small -> 1f
    ReaderStyle.Size.Medium -> 2f
    ReaderStyle.Size.Large -> 3f
    ReaderStyle.Size.Huge -> 4f
}

private fun Float.toSize() = when (this) {
    0f -> ReaderStyle.Size.Tiny
    1f -> ReaderStyle.Size.Small
    2f -> ReaderStyle.Size.Medium
    3f -> ReaderStyle.Size.Large
    4f -> ReaderStyle.Size.Huge
    else -> null
}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            ReaderOptionsUi(
                state = State(config = ReaderStyleConfig()) {}
            )
        }
    }
}
