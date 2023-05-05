/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import app.ss.auth.AuthRepository
import com.cryart.sabbathschool.account.AccountDialogFragment
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.quarterlies.QuarterliesActivity
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import com.cryart.sabbathschool.ui.about.AboutActivity
import com.cryart.sabbathschool.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ss.prefs.api.SSPrefs
import ss.settings.SettingsActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation for [AppNavigator]
 */
@Singleton
class AppNavigatorImpl @Inject constructor(
    private val ssPrefs: SSPrefs,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : AppNavigator, CoroutineScope by MainScope() {

    private suspend fun isSignedIn(): Boolean {
        return authRepository.getUser().getOrNull() != null
    }

    override fun navigate(activity: Activity, destination: Destination, extras: Bundle?) {
        launch(dispatcherProvider.io) {
            val clazz = getDestinationClass(destination) ?: return@launch
            val loginClass = LoginActivity::class.java

            val intent = if (clazz == loginClass || !isSignedIn()) {
                Intent(activity, loginClass).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            } else if (destination == Destination.ACCOUNT) {
                val fragment = AccountDialogFragment()
                val fm = (activity as? FragmentActivity)?.supportFragmentManager ?: return@launch
                fragment.show(fm, fragment.tag)
                return@launch
            } else {
                Intent(activity, clazz)
            }
            extras?.let {
                intent.putExtras(it)
            }

            when (destination) {
                Destination.LESSONS -> {
                    with(TaskStackBuilder.create(activity)) {
                        addNextIntent(QuarterliesActivity.launchIntent(activity))
                        addNextIntentWithParentStack(intent)
                        startActivities()
                    }
                }
                Destination.READ -> {
                    with(TaskStackBuilder.create(activity)) {
                        addNextIntent(QuarterliesActivity.launchIntent(activity))
                        ssPrefs.getLastQuarterlyIndex()?.let { index ->
                            addNextIntent(SSLessonsActivity.launchIntent(activity, index))
                        }
                        addNextIntentWithParentStack(intent)
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

    private fun getDestinationClass(destination: Destination): Class<*>? {
        return when (destination) {
            Destination.ABOUT -> AboutActivity::class.java
            Destination.ACCOUNT -> AccountDialogFragment::class.java
            Destination.LESSONS -> SSLessonsActivity::class.java
            Destination.LOGIN -> LoginActivity::class.java
            Destination.SETTINGS -> SettingsActivity::class.java
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
     * Navigate to either [SSLessonsActivity] or [SSReadingActivity]
     * depending on the uri from web (sabbath-school.adventech.io) received.
     *
     * If no quarterly index is found in the Uri we launch normal flow.
     *
     * Example links:
     * [1] https://sabbath-school.adventech.io/en/2021-03
     * [2] https://sabbath-school.adventech.io/en/2021-03/03/07-friday-further-thought/
     */
    private fun navigateFromWeb(activity: Activity, uri: Uri) = launch(dispatcherProvider.io) {
        if (!isSignedIn()) {
            val intent = Intent(activity, LoginActivity::class.java).apply {
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
        taskBuilder.addNextIntent(QuarterliesActivity.launchIntent(activity))

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

                taskBuilder.addNextIntent(SSLessonsActivity.launchIntent(activity, quarterlyIndex))
                endIntent = SSReadingActivity.launchIntent(activity, lessonIndex, readPosition)
            } else {
                endIntent = SSLessonsActivity.launchIntent(activity, quarterlyIndex)
            }

            with(taskBuilder) {
                addNextIntentWithParentStack(endIntent)
                startActivities()
            }
        } else {
            launchNormalFlow(activity)
        }
    }

    private fun launchNormalFlow(activity: Activity) {
        val intent = QuarterliesActivity.launchIntent(activity).apply {
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
