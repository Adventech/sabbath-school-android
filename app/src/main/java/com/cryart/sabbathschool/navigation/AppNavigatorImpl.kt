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
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.settings.SSSettingsActivity
import com.cryart.sabbathschool.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation for [AppNavigator]
 */
@Singleton
class AppNavigatorImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AppNavigator {

    private val isSignedIn: Boolean get() = firebaseAuth.currentUser != null

    override fun navigate(activity: Activity, destination: Destination) {
        val clazz = getDestinationClass(destination) ?: return
        val loginClass = LoginActivity::class.java

        val intent = if (clazz == loginClass || !isSignedIn) {
            Intent(activity, loginClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            Intent(activity, clazz)
        }
        activity.startActivity(intent)
    }

    override fun navigate(activity: Activity, deepLink: Uri) {
        val host = deepLink.authority ?: return
        val destination = Destination.fromKey(host) ?: return

        navigate(activity, destination)
    }

    private fun getDestinationClass(destination: Destination): Class<*>? {
        return when (destination) {
            Destination.LOGIN -> LoginActivity::class.java
            Destination.SETTINGS -> SSSettingsActivity::class.java
            else -> null
        }
    }
}
