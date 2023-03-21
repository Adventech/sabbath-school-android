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

package ss.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.auth.AuthRepository
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.list.DividerEntity
import app.ss.design.compose.extensions.list.ListEntity
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.icon.ResIcon
import app.ss.lessons.data.repository.user.UserDataRepository
import app.ss.models.config.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ss.settings.ui.prefs.PrefListEntity
import javax.inject.Inject
import app.ss.translations.R as L10nR

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository,
    private val appConfig: AppConfig
) : ViewModel() {

    private val _viewState: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState())
    internal val viewStateFlow: StateFlow<SettingsState> = _viewState

    private val isSwitchChecked = MutableStateFlow(false)

    fun deleteAccount() {
        _viewState.tryEmit(SettingsState(showProgress = true))

        viewModelScope.launch {
            authRepository.deleteAccount()
            userDataRepository.clear()

            _viewState.emit(SettingsState(false))
        }
    }

    fun inCompose() {
        val prefs = listOf(
            PrefListEntity.Section(
                ContentSpec.Res(L10nR.string.ss_settings_reminder),
                id = "section-reminder"
            ),
            PrefListEntity.Switch(
                icon = Icons.AlarmOff,
                title = ContentSpec.Res(L10nR.string.ss_settings_reminder),
                summary = ContentSpec.Res(L10nR.string.ss_settings_reminder_summary),
                id = "reminder-switch",
                checked = isSwitchChecked.value,
                onCheckChanged = { isSwitchChecked.tryEmit(it) }
            ),
            PrefListEntity.Generic(
                icon = Icons.Clock,
                title = ContentSpec.Res(L10nR.string.ss_settings_reminder_time),
                summary = ContentSpec.Str("08:00"),
                id = "reminder-entry",
                onClick = {}
            ),

            DividerEntity(id = "divider"),

            PrefListEntity.Section(
                ContentSpec.Res(L10nR.string.ss_about),
                id = "section-about"
            ),
            PrefListEntity.Generic(
                icon = Icons.Website,
                title = ContentSpec.Res(L10nR.string.ss_settings_website),
                summary = ContentSpec.Res(L10nR.string.ss_settings_website_summary),
                id = "website",
                onClick = {}
            ),
            PrefListEntity.Generic(
                icon = Icons.Facebook,
                title = ContentSpec.Res(L10nR.string.ss_settings_facebook),
                summary = ContentSpec.Res(L10nR.string.ss_settings_facebook_summary),
                id = "facebook",
                onClick = {}
            ),
            PrefListEntity.Generic(
                icon = ResIcon.Instagram,
                title = ContentSpec.Res(L10nR.string.ss_settings_instagram),
                summary = ContentSpec.Res(L10nR.string.ss_settings_instagram_summary),
                id = "instagram",
                onClick = {}
            ),
            PrefListEntity.Generic(
                icon = ResIcon.Github,
                title = ContentSpec.Res(L10nR.string.ss_settings_github),
                summary = ContentSpec.Res(L10nR.string.ss_settings_github_summary),
                id = "github",
                onClick = {}
            ),
            PrefListEntity.Generic(
                icon = Icons.VersionInfo,
                title = ContentSpec.Res(L10nR.string.ss_settings_version),
                summary = ContentSpec.Str(appConfig.version),
                id = "app-version",
                onClick = {}
            ),

            DividerEntity(id = "divider-two"),

            PrefListEntity.Section(
                ContentSpec.Res(L10nR.string.ss_account),
                id = "section-account"
            ),
            PrefListEntity.Generic(
                title = ContentSpec.Res(L10nR.string.ss_menu_sign_out),
                id = "account-sign-out",
                onClick = {},
                withWarning = true
            ),
            PrefListEntity.Generic(
                title = ContentSpec.Res(L10nR.string.ss_delete_account),
                id = "account-delete",
                onClick = {},
                withWarning = true
            ),
        )

        _viewState.tryEmit(SettingsState(prefListEntities = prefs))
    }
}

@Immutable
internal data class SettingsState(
    val authenticated: Boolean = true,
    val showProgress: Boolean = false,
    val prefListEntities: List<ListEntity> = emptyList()
)
