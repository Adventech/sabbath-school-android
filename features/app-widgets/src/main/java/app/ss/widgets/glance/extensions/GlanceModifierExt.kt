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

package app.ss.widgets.glance.extensions

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import com.cryart.sabbathschool.core.extensions.sdk.isAtLeastApi

@Composable
fun GlanceModifier.modifyAppWidgetBackground() = this
    .fillMaxSize()
    .appWidgetBackground()
    .background(GlanceTheme.colors.surface)
    .appWidgetBackgroundCornerRadius()

fun GlanceModifier.appWidgetBackgroundCornerRadius(): GlanceModifier {
    if (isAtLeastApi(Build.VERSION_CODES.S)) {
        cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        cornerRadius(16.dp)
    }
    return this
}

@Composable
fun GlanceModifier.divider(height: Dp = 0.5.dp) = this
    .fillMaxWidth()
    .height(height)
    .background(GlanceTheme.colors.inverseOnSurface)

private const val pkg = "com.cryart.sabbathschool"

internal val fallbackIntent: Intent = Intent("$pkg.ui.splash.SplashActivity").apply {
    `package` = pkg
}

internal fun Intent?.toAction(): Action =
    actionStartActivity(this ?: fallbackIntent)

fun GlanceModifier.clickable(intent: Intent?) = this.clickable (actionStartActivity(intent ?: fallbackIntent))
