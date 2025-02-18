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

package ss.settings.repository

import android.content.Context
import android.text.format.DateFormat
import app.ss.auth.AuthRepository
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.list.DividerEntity
import app.ss.design.compose.extensions.list.ListEntity
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.icon.ResIcon
import app.ss.models.config.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.lessons.api.ContentSyncProvider
import ss.misc.DateHelper
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs
import ss.prefs.model.ReminderTime
import ss.settings.DailyReminder
import ss.settings.ui.prefs.PrefListEntity
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import app.ss.translations.R as L10nR

interface SettingsRepository {

    fun entitiesFlow(onEntityClick: (SettingsEntity) -> Unit): Flow<List<ListEntity>>

    fun setReminderTime(hour: Int, minute: Int)

    fun signOut()

    fun deleteAccount()

    fun removeAllDownloads()
}

@Singleton
internal class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfig: AppConfig,
    private val authRepository: AuthRepository,
    private val dailyReminder: DailyReminder,
    private val dispatcherProvider: DispatcherProvider,
    private val prefs: SSPrefs,
    private val contentSyncProvider: ContentSyncProvider,
) : SettingsRepository, Scopable by ioScopable(dispatcherProvider) {

    override fun entitiesFlow(
        onEntityClick: (SettingsEntity) -> Unit
    ): Flow<List<ListEntity>> = combine(
        contentSyncProvider.hasDownloads(),
        prefs.reminderTimeFlow()
    ) { hasDownloads, reminderTime ->
        buildEntities(hasDownloads, reminderTime, onEntityClick)
    }

    private fun buildEntities(
        hasDownloads: Boolean,
        reminderTime: ReminderTime,
        onEntityClick: (SettingsEntity) -> Unit
    ): List<ListEntity> = listOf(
        PrefListEntity.Section(
            ContentSpec.Res(L10nR.string.ss_settings_reminder),
            id = "section-reminder"
        ),
        PrefListEntity.Switch(
            icon = if (prefs.reminderEnabled()) Icons.AlarmOn else Icons.AlarmOff,
            title = ContentSpec.Res(L10nR.string.ss_settings_reminder),
            summary = ContentSpec.Res(L10nR.string.ss_settings_reminder_summary),
            id = "reminder-switch",
            checked = prefs.reminderEnabled(),
            onCheckChanged = { checked ->
                prefs.setReminderEnabled(checked)
                dailyReminder.reSchedule()
                onEntityClick(SettingsEntity.Reminder.Switch(checked))
            }
        ),
        PrefListEntity.Generic(
            id = "reminder-time",
            enabled = prefs.reminderEnabled(),
            icon = Icons.Clock,
            title = ContentSpec.Res(L10nR.string.ss_settings_reminder_time),
            summary = ContentSpec.Str(reminderTime.formattedTime()),
            onClick = {
                onEntityClick(SettingsEntity.Reminder.Time(reminderTime.hour, reminderTime.min))
            }
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
            id = "about-website",
            onClick = { onEntityClick(SettingsEntity.About.Website) }
        ),
        PrefListEntity.Generic(
            icon = Icons.Facebook,
            title = ContentSpec.Res(L10nR.string.ss_settings_facebook),
            summary = ContentSpec.Res(L10nR.string.ss_settings_facebook_summary),
            id = "about-facebook",
            onClick = {
                onEntityClick(SettingsEntity.About.Facebook)
            }
        ),
        PrefListEntity.Generic(
            icon = ResIcon.Instagram,
            title = ContentSpec.Res(L10nR.string.ss_settings_instagram),
            summary = ContentSpec.Res(L10nR.string.ss_settings_instagram_summary),
            id = "about-instagram",
            onClick = {
                onEntityClick(SettingsEntity.About.Instagram)
            }
        ),
        PrefListEntity.Generic(
            icon = ResIcon.Github,
            title = ContentSpec.Res(L10nR.string.ss_settings_github),
            summary = ContentSpec.Res(L10nR.string.ss_settings_github_summary),
            id = "about-github",
            onClick = {
                onEntityClick(SettingsEntity.About.Github)
            }
        ),
        PrefListEntity.Generic(
            icon = Icons.VersionInfo,
            title = ContentSpec.Res(L10nR.string.ss_settings_version),
            summary = ContentSpec.Str(appConfig.version),
            id = "about-version",
            onClick = {
                onEntityClick(SettingsEntity.About.Version)
            }
        ),

        PrefListEntity.Generic(
            icon = Icons.Privacy,
            title = ContentSpec.Res(L10nR.string.ss_privacy_policy),
            summary = ContentSpec.Res(L10nR.string.ss_settings_privacy_policy_summary),
            id = "about-privacy-policy",
            onClick = {
                onEntityClick(SettingsEntity.About.Policy)
            }
        ),

        DividerEntity(id = "divider-two"),

        PrefListEntity.Section(
            ContentSpec.Res(L10nR.string.ss_account),
            id = "section-account"
        ),
        PrefListEntity.Generic(
            id = "downloads-delete",
            enabled = hasDownloads,
            title = ContentSpec.Res(L10nR.string.ss_delete_downloads),
            onClick = { onEntityClick(SettingsEntity.Account.DeleteContent) },
            withWarning = true
        ),
        PrefListEntity.Generic(
            title = ContentSpec.Res(L10nR.string.ss_menu_sign_out),
            id = "account-sign-out",
            onClick = { onEntityClick(SettingsEntity.Account.SignOut) },
            withWarning = true
        ),
        PrefListEntity.Generic(
            title = ContentSpec.Res(L10nR.string.ss_delete_account),
            id = "account-delete",
            onClick = { onEntityClick(SettingsEntity.Account.Delete) },
            withWarning = true
        ),
    )

    override fun setReminderTime(hour: Int, minute: Int) {
        prefs.setReminderTime(ReminderTime(hour, minute))

        dailyReminder.reSchedule()
    }

    override fun signOut() {
        scope.launch {
            prefs.clear()
            authRepository.logout()
        }
    }

    override fun deleteAccount() {
        scope.launch {
            authRepository.deleteAccount()
            prefs.clear()
        }
    }

    override fun removeAllDownloads() {
        scope.launch { contentSyncProvider.removeAllDownloads() }
    }

    private fun ReminderTime.formattedTime(): String {
        return DateHelper.parseTimeAndReturnInFormat(
            String.format(Locale.getDefault(), "%02d:%02d", hour, min),
            SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT,
            DateFormat.getTimeFormat(context)
        )
    }

}
