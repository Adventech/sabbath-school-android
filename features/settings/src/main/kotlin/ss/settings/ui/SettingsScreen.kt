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

package ss.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.scaffold.SsScaffold
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.overlay.OverlayHost
import ss.circuit.helpers.overlay.DialogOverlay
import ss.settings.SettingsScreen.Event
import ss.settings.SettingsScreen.Overlay
import ss.settings.SettingsScreen.State
import app.ss.translations.R as L10nR

@Composable
internal fun SettingsUiScreen(
    state: State,
    modifier: Modifier = Modifier
) {
    OverlayContent(state = state)

    @OptIn(ExperimentalMaterial3Api::class)
    SsScaffold(
        modifier = modifier,
        topBar = {
            SsTopAppBar(
                spec = TopAppBarSpec(
                    topAppBarType = TopAppBarType.Small
                ),
                title = { Text(text = stringResource(id = L10nR.string.ss_settings)) },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSick(Event.NavBack)
                    }) {
                        IconBox(icon = Icons.ArrowBack)
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = state.entities,
                key = { it.id }
            ) { item -> item.Content() }
        }
    }
}


@Composable
private fun OverlayContent(state: State) {
    val overlay = state.overlay ?: return
    val overlayHost = LocalOverlayHost.current

    when (overlay) {
        Overlay.ConfirmDeleteAccount -> overlayHost.ConfirmAccountDelete(
            overlay = overlay,
            eventSick = state.eventSick
        )
        is Overlay.SelectReminderTime -> overlayHost.ShowTimePicker(
            overlay = overlay,
            eventSick = state.eventSick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverlayHost.ShowTimePicker(
    overlay: Overlay.SelectReminderTime,
    eventSick: (Event) -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = overlay.hour,
        initialMinute = overlay.minute,
    )

    LaunchedEffect(overlay) {
        show(
            DialogOverlay<Unit>(
                title = ContentSpec.Res(L10nR.string.ss_settings_reminder_time),
                cancelButton = DialogOverlay.Button(
                    title = ContentSpec.Res(android.R.string.cancel)
                ) { eventSick(Event.OverlayDismiss) },
                confirmButton = DialogOverlay.Button(
                    title = ContentSpec.Res(android.R.string.ok)
                ) {
                    eventSick(Event.SetReminderTime(timePickerState.hour, timePickerState.minute))
                }
            ) {
                TimeInput(
                    state = timePickerState,
                    modifier = Modifier
                )
            }
        )
    }
}

@Composable
private fun OverlayHost.ConfirmAccountDelete(
    overlay: Overlay?,
    eventSick: (Event) -> Unit,
) {
    LaunchedEffect(overlay) {
        show(
            DialogOverlay<Unit>(
                title = ContentSpec.Res(L10nR.string.ss_delete_account_question),
                cancelButton = DialogOverlay.Button(
                    title = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_negative)
                ) { eventSick(Event.OverlayDismiss) },
                confirmButton = DialogOverlay.Button(
                    title = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_positive)
                ) {  }
            ) {
                Text(
                    text = stringResource(id = L10nR.string.ss_delete_account_warning),
                    style = SsTheme.typography.bodyMedium
                )
            }
        )
    }
}
