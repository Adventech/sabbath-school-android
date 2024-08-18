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

package app.ss.design.compose.widget.icon

import androidx.annotation.StringRes
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Facebook
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons as MaterialIcons
import app.ss.translations.R.string as L10n

enum class Icons(
    private val imageVector: ImageVector,
    @StringRes private val contentDescription: Int?
) : IconSlot {

    AccountCircle(MaterialIcons.Rounded.AccountCircle, L10n.ss_account),
    AlarmOff(MaterialIcons.Rounded.AlarmOff, L10n.ss_settings_reminder),
    AlarmOn(MaterialIcons.Rounded.AlarmOn, L10n.ss_settings_reminder),
    ArrowBack(MaterialIcons.AutoMirrored.Rounded.ArrowBack, L10n.ss_action_arrow_back),
    ArrowDropDown(MaterialIcons.Rounded.ArrowDropDown, L10n.ss_action_arrow_drop_down),
    ArrowRight(MaterialIcons.AutoMirrored.Rounded.KeyboardArrowRight, L10n.ss_action_arrow_right),
    Cancel(MaterialIcons.Rounded.Cancel, android.R.string.cancel),
    Check(MaterialIcons.Rounded.Check, L10n.ss_action_selected),
    Clock(MaterialIcons.Rounded.AccessTime, L10n.ss_settings_reminder_time),
    Close(MaterialIcons.Rounded.Close, L10n.ss_action_close),
    Facebook(MaterialIcons.Rounded.Facebook, L10n.ss_about_facebook),
    Info(MaterialIcons.Rounded.Info, L10n.ss_about),
    Search(MaterialIcons.Rounded.Search, android.R.string.search_go),
    Settings(MaterialIcons.Rounded.Settings, L10n.ss_settings),
    Share(MaterialIcons.Rounded.Share, L10n.ss_share),

    Home(MaterialIcons.Outlined.Home, L10n.ss_app_name),
    HomeFilled(MaterialIcons.Filled.Home, L10n.ss_app_name),
    VersionInfo(MaterialIcons.Rounded.Info, L10n.ss_settings_version),
    Website(MaterialIcons.Rounded.Web, L10n.ss_settings_website),
    Privacy(MaterialIcons.Rounded.PrivacyTip, null),
    ;

    @Composable
    override fun Content(contentColor: Color, modifier: Modifier) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription?.let { stringResource(id = it) },
            tint = contentColor,
            modifier = modifier
        )
    }
}
