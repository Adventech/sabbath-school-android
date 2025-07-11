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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.ResIcon
import app.ss.design.compose.widget.material.LegacySlider
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.style.ReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.Styler
import ss.document.reader.ReaderOptionsScreen.Event
import ss.document.reader.ReaderOptionsScreen.State
import app.ss.translations.R as L10nR
import ss.document.R as DocumentR

@CircuitInject(ReaderOptionsScreen::class, SingletonComponent::class)
@Composable
fun ReaderOptionsUi(state: State, modifier: Modifier = Modifier) {
    val hapticFeedback = LocalSsHapticFeedback.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReaderOptionsTheme(state.config.theme) {
            hapticFeedback.performSegmentSwitch()
            state.eventSink(Event.OnThemeChanged(it))
        }

        ReaderOptionsTypeface(state.config.typeface) {
            hapticFeedback.performSegmentSwitch()
            state.eventSink(Event.OnTypefaceChanged(it))
        }

        ReaderOptionsFontSize(state.config.size) {
            hapticFeedback.performGestureEnd()
            state.eventSink(Event.OnFontSizeChanged(it))
        }
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

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        ReaderStyle.Theme.entries.forEachIndexed { index, item ->
            SegmentedButton(
                selected = theme == item,
                onClick = { onSelected(item) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ReaderStyle.Typeface.entries.size)
            ) {
                Text(
                    text = stringResource(item.label()),
                    style = SsTheme.typography.titleMedium
                )
            }
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

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        ReaderStyle.Typeface.entries.forEachIndexed { index, item ->
            SegmentedButton(
                selected = typeface == item,
                onClick = { onSelected(item) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ReaderStyle.Typeface.entries.size)
            ) {
                Text(
                    text = stringResource(item.label()),
                    style = SsTheme.typography.titleMedium.copy(
                        fontFamily = Styler.fontFamily(item)
                    )
                )
            }
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

        IconBox(
            icon = ResIcon(
                DocumentR.drawable.ic_text_format,
                contentDescription = null
            ),
            modifier = Modifier.size(14.dp),
            contentColor = SsTheme.colors.primary
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

        IconBox(
            icon = ResIcon(
                DocumentR.drawable.ic_text_format,
                contentDescription = null
            ),
            modifier = Modifier.size(32.dp),
            contentColor = SsTheme.colors.primary
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
