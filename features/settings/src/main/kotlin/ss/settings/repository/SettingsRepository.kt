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

package ss.settings.repository

import android.content.Context
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.list.DividerEntity
import app.ss.design.compose.extensions.list.ListEntity
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.icon.ResIcon
import app.ss.models.config.AppConfig
import app.ss.translations.R
import dagger.hilt.android.qualifiers.ApplicationContext
import ss.prefs.api.SSPrefs
import ss.settings.DailyReminder
import ss.settings.ui.prefs.PrefListEntity
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

internal interface SettingsRepository {
    fun buildEntities(
        onCheckedChange: (Boolean) -> Unit,
        onGoToUrl: (String) -> Unit
    ): List<ListEntity>
}

@Singleton
internal class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfig: AppConfig,
    private val prefs: SSPrefs,
    private val dailyReminder: DailyReminder,
) : SettingsRepository {

    override fun buildEntities(
        onCheckedChange: (Boolean) -> Unit,
        onGoToUrl: (String) -> Unit
    ): List<ListEntity> = listOf(
        PrefListEntity.Section(
            ContentSpec.Res(R.string.ss_settings_reminder),
            id = "section-reminder"
        ),
        PrefListEntity.Switch(
            icon = if (prefs.reminderEnabled()) Icons.AlarmOn else Icons.AlarmOff,
            title = ContentSpec.Res(R.string.ss_settings_reminder),
            summary = ContentSpec.Res(R.string.ss_settings_reminder_summary),
            id = "reminder-switch",
            checked = prefs.reminderEnabled(),
            onCheckChanged = {
                prefs.setReminderEnabled(it)
                dailyReminder.reSchedule()
                onCheckedChange(it)
            }
        ),
        PrefListEntity.Generic(
            icon = Icons.Clock,
            title = ContentSpec.Res(R.string.ss_settings_reminder_time),
            summary = ContentSpec.Str(formattedReminderTime()),
            id = "reminder-entry",
            onClick = {}
        ),

        DividerEntity(id = "divider"),

        PrefListEntity.Section(
            ContentSpec.Res(R.string.ss_about),
            id = "section-about"
        ),
        PrefListEntity.Generic(
            icon = Icons.Website,
            title = ContentSpec.Res(R.string.ss_settings_website),
            summary = ContentSpec.Res(R.string.ss_settings_website_summary),
            id = "website",
            onClick = {
                onGoToUrl(context.getString(R.string.ss_settings_website_url))
            }
        ),
        PrefListEntity.Generic(
            icon = Icons.Facebook,
            title = ContentSpec.Res(R.string.ss_settings_facebook),
            summary = ContentSpec.Res(R.string.ss_settings_facebook_summary),
            id = "facebook",
            onClick = {
                onGoToUrl(context.getString(R.string.ss_settings_facebook_url))
            }
        ),
        PrefListEntity.Generic(
            icon = ResIcon.Instagram,
            title = ContentSpec.Res(R.string.ss_settings_instagram),
            summary = ContentSpec.Res(R.string.ss_settings_instagram_summary),
            id = "instagram",
            onClick = {
                onGoToUrl(context.getString(R.string.ss_settings_instagram_url))
            }
        ),
        PrefListEntity.Generic(
            icon = ResIcon.Github,
            title = ContentSpec.Res(R.string.ss_settings_github),
            summary = ContentSpec.Res(R.string.ss_settings_github_summary),
            id = "github",
            onClick = {
                onGoToUrl(context.getString(R.string.ss_settings_github_url))
            }
        ),
        PrefListEntity.Generic(
            icon = Icons.VersionInfo,
            title = ContentSpec.Res(R.string.ss_settings_version),
            summary = ContentSpec.Str(appConfig.version),
            id = "app-version",
            onClick = {
                onGoToUrl(context.getString(R.string.ss_app_playstore_url))
            }
        ),

        DividerEntity(id = "divider-two"),

        PrefListEntity.Section(
            ContentSpec.Res(R.string.ss_account),
            id = "section-account"
        ),
        PrefListEntity.Generic(
            title = ContentSpec.Res(R.string.ss_menu_sign_out),
            id = "account-sign-out",
            onClick = {},
            withWarning = true
        ),
        PrefListEntity.Generic(
            title = ContentSpec.Res(R.string.ss_delete_account),
            id = "account-delete",
            onClick = {},
            withWarning = true
        ),
    )

    private fun formattedReminderTime(): String {
        val time = prefs.getReminderTime()
        return String.format(Locale.getDefault(), "%02d:%02d", time.hour, time.min)
    }

}
