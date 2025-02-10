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
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.ui.about.AboutActivity
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.mainScopable
import ss.libraries.circuit.navigation.DocumentScreen
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.ResourceScreen
import ss.services.circuit.impl.CircuitActivity
import javax.inject.Inject
import javax.inject.Singleton

/** Implementation for [AppNavigator] */
@Singleton
class AppNavigatorImpl
@Inject
constructor(
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

            activity.startActivity(intent)
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
     * Navigate to either [ResourceScreen] or [DocumentScreen]
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

        val endIntent: Intent
        val taskBuilder = TaskStackBuilder.create(activity)
        taskBuilder.addNextIntent(screenIntent(activity, HomeNavScreen))

        val model = parseWebUrl(uri.toString()) ?: return@launch launchNormalFlow(activity)

        if (model.documentIndex != null) {
            taskBuilder.addNextIntent(screenIntent(activity, ResourceScreen(model.resourceIndex)))
            endIntent = screenIntent(activity, DocumentScreen(model.documentIndex, null))
        } else {
            endIntent = screenIntent(
                activity, ResourceScreen(model.resourceIndex)
            )
        }

        with(taskBuilder) {
            addNextIntent(endIntent)
            startActivities()
        }
    }

    private fun parseWebUrl(url: String): NavigationModel? {
        val regex = Regex("https://sabbath-school(?:-stage)?\\.adventech\\.io(?:/resources)?/([a-z]{2})(?:/([a-z]+))?/([\\w-]+)(?:/(.*))?")
        val matchResult = regex.matchEntire(url)

        return if (matchResult != null) {
            val (language, categoryOpt, resource, document) = matchResult.destructured
            val category = if (categoryOpt.isNotEmpty()) categoryOpt else "ss"
            val resourceIndex = "$language/$category/$resource"
            val documentIndex = if (document.isNotEmpty()) "$resourceIndex/${document.substringBefore("/")}" else null

            NavigationModel(resourceIndex, documentIndex)
        } else {
            null
        }
    }

    private fun launchNormalFlow(activity: Activity) {
        val intent = screenIntent(activity, HomeNavScreen).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
    }
}

private data class NavigationModel(
    val resourceIndex: String,
    val documentIndex: String?
)
