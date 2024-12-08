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

package com.cryart.sabbathschool.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.core.os.bundleOf
import app.ss.auth.AuthRepository
import app.ss.readings.SSReadingActivity
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.ui.about.AboutActivity
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.mainScopable
import ss.libraries.circuit.navigation.LessonsScreen
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.QuarterliesScreen
import ss.prefs.api.SSPrefs
import ss.services.circuit.impl.CircuitActivity
import javax.inject.Inject
import javax.inject.Singleton

/** Implementation for [AppNavigator] */
@Singleton
class AppNavigatorImpl
@Inject
constructor(
    private val ssPrefs: SSPrefs,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : AppNavigator, Scopable by mainScopable(dispatcherProvider) {

    private suspend fun isSignedIn(): Boolean {
        return authRepository.getUser().getOrNull() != null
    }

    override fun navigate(activity: Activity, destination: Destination, extras: Bundle?) {
        scope.launch(dispatcherProvider.default) {
            val clazz = getDestinationClass(destination) ?: return@launch

            val intent = if (!isSignedIn()) {
                screenIntent(activity, LoginScreen).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            } else {
                Intent(activity, clazz)
            }
            extras?.let {
                intent.putExtras(it)
            }

            when (destination) {
                Destination.READ -> {
                    with(TaskStackBuilder.create(activity)) {
                        addNextIntent(screenIntent(activity, QuarterliesScreen()))
                        ssPrefs.getLastQuarterlyIndex()?.let { index ->
                            addNextIntent(screenIntent(activity, LessonsScreen(index)))
                        }
                        addNextIntent(intent)
                        startActivities()
                    }
                }
                else -> {
                    activity.startActivity(intent)
                }
            }
        }
    }

    override fun navigate(activity: Activity, deepLink: Uri) {
        val host = deepLink.host ?: return
        val destination = Destination.fromKey(host) ?: return

        if (destination == Destination.READ_WEB) {
            navigateFromWeb(activity, deepLink)
        } else {
            navigate(activity, destination, getExtras(deepLink))
        }
    }

    override fun navigate(context: Context, screen: Screen) {
        CircuitActivity.launch(context, screen)
    }

    override fun screenIntent(context: Context, screen: Screen): Intent {
        return CircuitActivity.launchIntent(context, screen)
    }

    private fun getDestinationClass(destination: Destination): Class<*>? {
        return when (destination) {
            Destination.ABOUT -> AboutActivity::class.java
            Destination.READ -> SSReadingActivity::class.java
            else -> null
        }
    }

    private fun getExtras(uri: Uri): Bundle {
        val pairs = uri.queryParameterNames.map { key ->
            key to uri.getQueryParameter(key)
        }.toTypedArray()

        return bundleOf(*pairs)
    }

    /**
     * Navigate to either [LessonsScreen] or [SSReadingActivity]
     * depending on the uri from web (sabbath-school.adventech.io) received.
     *
     * If no quarterly index is found in the Uri we launch normal flow.
     *
     * Example links:
     * [1] https://sabbath-school.adventech.io/en/2021-03
     * [2] https://sabbath-school.adventech.io/en/2021-03/03/07-friday-further-thought/
     */
    private fun navigateFromWeb(activity: Activity, uri: Uri) = scope.launch(dispatcherProvider.io) {
        if (!isSignedIn()) {
            val intent = screenIntent(activity, LoginScreen).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            activity.startActivity(intent)
            return@launch
        }

        val segments = uri.pathSegments
        val quarterlyIndex: String
        val lessonIndex: String
        val endIntent: Intent
        val taskBuilder = TaskStackBuilder.create(activity)
        taskBuilder.addNextIntent(screenIntent(activity, QuarterliesScreen()))

        if (uri.path?.matches(WEB_LINK_REGEX.toRegex()) == true && segments.size >= 2) {
            quarterlyIndex = "${segments.first()}-${segments[1]}"

            if (segments.size > 2) {
                lessonIndex = "$quarterlyIndex-${segments[2]}"

                val readPosition = if (segments.size > 3) {
                    val dayNumber = segments[3].filter { it.isDigit() }
                    val index = dayNumber.toIntOrNull()?.minus(1)
                    index?.toString()
                } else {
                    null
                }

                taskBuilder.addNextIntent(screenIntent(activity, LessonsScreen(quarterlyIndex)))
                endIntent = SSReadingActivity.launchIntent(activity, lessonIndex, readPosition)
            } else {
                endIntent = screenIntent(activity, LessonsScreen(quarterlyIndex))
            }

            with(taskBuilder) {
                addNextIntent(endIntent)
                startActivities()
            }
        } else {
            launchNormalFlow(activity)
        }
    }

    private fun launchNormalFlow(activity: Activity) {
        val intent = screenIntent(activity, QuarterliesScreen()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
    }

    companion object {
        private const val WEB_LINK_REGEX =
            "(^\\/[a-z]{2,}\\/?\$)|(^\\/[a-z]{2,}\\/\\d{4}-\\d{2}(-[a-z]{2})?\\/?\$)|(^\\/[a-z]{2,}\\/\\d{4}-\\d{2}(-[a-z]{2})?\\/\\d{2}\\/?\$)|" +
                "(^\\/[a-z]{2,}\\/\\d{4}-\\d{2}(-[a-z]{2})?\\/\\d{2}\\/\\d{2}(-.+)?\\/?\$)"
    }
}
