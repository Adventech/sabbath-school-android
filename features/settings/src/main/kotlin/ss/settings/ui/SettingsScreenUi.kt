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

package ss.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.scaffold.HazeScaffold
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuitx.overlays.DialogResult
import dagger.hilt.components.SingletonComponent
import ss.libraries.circuit.navigation.SettingsScreen
import ss.libraries.circuit.overlay.ssAlertDialogOverlay
import ss.settings.Event
import ss.settings.Overlay
import ss.settings.State
import app.ss.translations.R as L10nR

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(SettingsScreen::class, SingletonComponent::class)
@Composable
fun SettingsScreenUi(
    state: State,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hapticFeedback = LocalSsHapticFeedback.current

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = L10nR.string.ss_settings)) },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticFeedback.performClick()
                        state.eventSick(Event.NavBack)
                    }) {
                        IconBox(icon = Icons.ArrowBack)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                )
            )
        },
        blurTopBar = true
    ) { contentPadding ->

        LazyColumn(
            modifier = Modifier,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = state.entities,
                key = { it.id }
            ) { item -> item.Content() }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }

    OverlayContent(state = state)

    LaunchedEffect(state.overlay) {
        if (state.overlay != null) {
            hapticFeedback.performScreenView()
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

        Overlay.ConfirmRemoveDownloads -> overlayHost.ShowConfirmRemoveDownloads(
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
    val hapticFeedback = LocalSsHapticFeedback.current
    val timePickerState = rememberTimePickerState(
        initialHour = overlay.hour,
        initialMinute = overlay.minute,
    )

    LaunchedEffect(overlay) {
        val result = show(
            ssAlertDialogOverlay(
                title = ContentSpec.Res(L10nR.string.ss_settings_reminder_time),
                cancelText = ContentSpec.Res(android.R.string.cancel),
                confirmText = ContentSpec.Res(android.R.string.ok),
                content = {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier
                    )
                }
            )
        )
        when (result) {
            DialogResult.Confirm -> {
                eventSick(Event.SetReminderTime(timePickerState.hour, timePickerState.minute))
                hapticFeedback.performSuccess()
            }
            DialogResult.Cancel,
            DialogResult.Dismiss -> {
                eventSick(Event.OverlayDismiss)
                hapticFeedback.performClick()
            }
        }
    }
}

@Composable
private fun OverlayHost.ConfirmAccountDelete(
    overlay: Overlay?,
    eventSick: (Event) -> Unit,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    LaunchedEffect(overlay) {
        val result = show(
            ssAlertDialogOverlay(
                title = ContentSpec.Res(L10nR.string.ss_delete_account_question),
                cancelText = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_negative),
                confirmText = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_positive),
                content = {
                    Text(
                        text = stringResource(id = L10nR.string.ss_delete_account_warning),
                        style = SsTheme.typography.bodyMedium
                    )
                }
            )
        )

        when (result) {
            DialogResult.Confirm -> {
                hapticFeedback.performError()
                eventSick(Event.AccountDeleteConfirmed)
            }
            DialogResult.Cancel,
            DialogResult.Dismiss -> {
                hapticFeedback.performSuccess()
                eventSick(Event.OverlayDismiss)
            }
        }
    }
}

@Composable
private fun OverlayHost.ShowConfirmRemoveDownloads(
    overlay: Overlay?,
    eventSick: (Event) -> Unit,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    LaunchedEffect(overlay) {
        val result = show(
            ssAlertDialogOverlay(
                title = ContentSpec.Res(L10nR.string.ss_delete_downloads),
                cancelText = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_negative),
                confirmText = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_positive),
                content = {
                    Text(
                        text = stringResource(id = L10nR.string.ss_delete_downloads_confirm),
                        style = SsTheme.typography.bodyMedium
                    )
                }
            )
        )
        when (result) {
            DialogResult.Confirm -> {
                hapticFeedback.performError()
                eventSick(Event.RemoveDownloads)
            }
            DialogResult.Cancel,
            DialogResult.Dismiss -> {
                hapticFeedback.performSuccess()
                eventSick(Event.OverlayDismiss)
            }
        }
    }
}
