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

import androidx.annotation.StringRes
import app.ss.translations.R as L10nR

internal sealed interface SettingsEntity {

    sealed class About(@StringRes val resId: Int) : SettingsEntity {
        data object Facebook : About(L10nR.string.ss_settings_facebook_url)
        data object Github : About(L10nR.string.ss_settings_github_url)
        data object Instagram : About(L10nR.string.ss_settings_instagram_url)
        data object Version : About(L10nR.string.ss_app_playstore_url)
        data object Website : About(L10nR.string.ss_settings_website_url)
    }

    sealed interface Account: SettingsEntity {
        data object Delete : Account
        data object DeleteContent : Account
        data object SignOut : Account
    }

    sealed interface Reminder : SettingsEntity {
        data class Switch(val isChecked: Boolean): Reminder
        data class Time(val hour: Int, val minute: Int) : Reminder
    }

}
