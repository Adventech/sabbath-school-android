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

package ss.navigation.suite

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import ss.libraries.circuit.navigation.QuarterliesScreen
import ss.libraries.circuit.navigation.SettingsScreen
import app.ss.translations.R as L10nR

enum class NavbarItem(@DrawableRes val iconRes: Int, @StringRes val title: Int) {
    SabbathSchool(R.drawable.ss_ic_sabbath_school, L10nR.string.ss_app_name),
    AliveInJesus(R.drawable.ss_ic_aij, L10nR.string.ss_alive_in_jesus),
    PersonalMinistries(R.drawable.ss_ic_pm, L10nR.string.ss_personal_ministries),
    Devotionals(R.drawable.ss_ic_devotion, L10nR.string.ss_devotionals),
    Account(R.drawable.ss_ic_profile, L10nR.string.ss_account),
}

fun NavbarItem.screen(): Screen = when (this) {
    NavbarItem.SabbathSchool -> QuarterliesScreen
    NavbarItem.AliveInJesus -> AliveInJesusScreen
    NavbarItem.PersonalMinistries -> PersonalMinistriesScreen
    NavbarItem.Devotionals -> DevotionalsScreen
    NavbarItem.Account -> SettingsScreen
}

@Parcelize data object AliveInJesusScreen : Screen
@Parcelize data object PersonalMinistriesScreen : Screen
@Parcelize data object DevotionalsScreen : Screen
