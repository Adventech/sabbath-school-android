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

package ss.settings.ui.prefs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.ContentAlpha
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.content.asText
import app.ss.design.compose.extensions.previews.DayNightPreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.translations.R as L10nR

@Composable
internal fun PreferenceItem(
    item: PrefListEntity,
    modifier: Modifier = Modifier
) {
    val (icon, title, summary) = remember(item) {
        when (item) {
            is PrefListEntity.Generic -> Triple(item.icon, item.title, item.summary)
            is PrefListEntity.Section -> Triple(null, item.title, null)
            is PrefListEntity.Switch -> Triple(item.icon, item.title, item.summary)
        }
    }
    val isSection = remember(item) { item is PrefListEntity.Section }

    val contentAlpha = remember(item) {
        if (item.enabled) ContentAlpha.high else ContentAlpha.disabled
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                when (item) {
                    is PrefListEntity.Switch -> {
                        Modifier.toggleable(
                            value = item.checked,
                            enabled = item.enabled,
                            role = Role.Switch,
                            onValueChange = item.onCheckChanged
                        )
                    }
                    is PrefListEntity.Generic -> {
                        Modifier.clickable(
                            enabled = item.enabled,
                            onClickLabel = item.title.asText(),
                            role = Role.Button,
                            onClick = item.onClick,
                        )
                    }
                    else -> Modifier
                }
            )
            .padding(horizontal = 12.dp)
            .then(
                if (isSection) {
                    Modifier.padding(top = 8.dp)
                } else {
                    Modifier.padding(vertical = 10.dp)
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        icon?.let {
            IconBox(
                icon = icon,
                modifier = Modifier.alpha(contentAlpha)
            )
        }

        Column(
            modifier = Modifier.weight(1.0f),
        ) {

            Text(
                text = title.asText(),
                style = SsTheme.typography.bodyMedium.copy(
                    fontSize = if (isSection) 13.sp else 16.sp,
                    fontWeight = if (isSection) FontWeight.Bold else FontWeight.Normal
                ),
                color = if ((item as? PrefListEntity.Generic)?.withWarning == true) SsTheme.colors.error else SsTheme.colors.primaryForeground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(contentAlpha),

                )
            summary?.let {
                Text(
                    text = summary.asText(),
                    color = SsTheme.colors.secondaryForeground,
                    style = SsTheme.typography.bodySmall,
                    modifier = Modifier.alpha(contentAlpha)
                )
            }
        }

        if (item is PrefListEntity.Switch) {
            Switch(
                checked = item.checked,
                onCheckedChange = item.onCheckChanged,
                enabled = item.enabled
            )
        }

    }
}

@Composable
@DayNightPreviews
private fun PreviewItem() {
    SsTheme {
        Surface {
            LazyColumn {
                item {
                    PreferenceItem(
                        item = PrefListEntity.Section(
                            title = ContentSpec.Res(L10nR.string.ss_settings_reminder),
                            id = "reminder"
                        )
                    )
                }
                item {
                    PreferenceItem(
                        item = PrefListEntity.Generic(
                            icon = Icons.Clock,
                            title = ContentSpec.Res(L10nR.string.ss_settings_reminder_time),
                            summary = ContentSpec.Str("8:00 am"),
                            id = "reminder-entry",
                            onClick = {}
                        )
                    )
                }

                item {
                    var isChecked by remember { mutableStateOf(false) }

                    PreferenceItem(
                        item = PrefListEntity.Switch(
                            icon = if (isChecked) Icons.AlarmOn else Icons.AlarmOff,
                            title = ContentSpec.Res(L10nR.string.ss_settings_reminder),
                            summary = ContentSpec.Res(L10nR.string.ss_settings_reminder_summary),
                            checked = isChecked,
                            onCheckChanged = { isChecked = it },
                            id = "switch"
                        )
                    )
                }
            }
        }
    }
}
