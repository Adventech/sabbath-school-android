package ss.settings.ui.prefs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.ContentAlpha
import app.ss.design.compose.extensions.content.asText
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import com.cryart.design.R

/**
 * A composable that displays a section header.
 *
 * @param item The section item to display.
 * @param modifier The modifier to be applied to the header.
 */
@Composable
internal fun SectionHeader(
    item: PrefListEntity.Section,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = if (item.enabled) ContentAlpha.high else ContentAlpha.disabled
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = item.title.asText(),
            style = SsTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            color = SsTheme.colors.primary,
            modifier = Modifier.alpha(contentAlpha)
        )
    }
}

/**
 * A composable that displays a generic preference item.
 *
 * @param item The generic preference item to display.
 * @param modifier The modifier to be applied to the item.
 */
@Composable
internal fun GenericPreference(
    item: PrefListEntity.Generic,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    val contentAlpha = if (item.enabled) ContentAlpha.high else ContentAlpha.disabled

    PreferenceRow(
        modifier = modifier
            .clickable(
                enabled = item.enabled,
                onClickLabel = item.title.asText(),
                role = Role.Button,
                onClick = {
                    hapticFeedback.performClick()
                    item.onClick()
                },
            ),
        icon = item.icon?.let {
            { IconBox(icon = it, modifier = Modifier.alpha(contentAlpha)) }
        },
        title = {
            Text(
                text = item.title.asText(),
                color = if (item.withWarning) SsTheme.colors.error else SsTheme.colors.primaryForeground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(contentAlpha),
            )
        },
        summary = item.summary?.let {
            {
                Text(
                    text = it.asText(),
                    color = SsTheme.colors.secondaryForeground,
                    style = SsTheme.typography.bodySmall,
                    modifier = Modifier.alpha(contentAlpha)
                )
            }
        }
    )
}

/**
 * A composable that displays a switch preference item.
 *
 * @param item The switch preference item to display.
 * @param modifier The modifier to be applied to the item.
 */
@Composable
internal fun SwitchPreference(
    item: PrefListEntity.Switch,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    val contentAlpha = if (item.enabled) ContentAlpha.high else ContentAlpha.disabled

    PreferenceRow(
        modifier = modifier
            .toggleable(
                value = item.checked,
                enabled = item.enabled,
                role = Role.Switch,
                onValueChange = {
                    hapticFeedback.performToggleSwitch(it)
                    item.onCheckChanged(it)
                }
            ),
        icon = item.icon?.let {
            { IconBox(icon = it, modifier = Modifier.alpha(contentAlpha)) }
        },
        title = {
            Text(
                fontSize = 16.sp,
                text = item.title.asText(),
                style = SsTheme.typography.bodyMedium,
                color = SsTheme.colors.primaryForeground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(contentAlpha),
            )
        },
        summary = item.summary?.let {
            {
                Text(
                    text = it.asText(),
                    color = SsTheme.colors.secondaryForeground,
                    style = SsTheme.typography.bodySmall,
                    modifier = Modifier.alpha(contentAlpha)
                )
            }
        },
        action = {
            Switch(
                checked = item.checked,
                onCheckedChange = null, // Handled by toggleable
                enabled = item.enabled,
                thumbContent = if (item.checked) {
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_done),
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        }
    )
}
