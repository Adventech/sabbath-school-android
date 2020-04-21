/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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
package com.cryart.sabbathschool.misc

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import java.util.HashMap

object SSEvent {

    @JvmStatic
    fun track(context: Context, event_name: String) {
        track(context, event_name, HashMap<String, Any>())
    }

    @JvmStatic
    fun track(context: Context, event_name: String, values: HashMap<String, *>) {
        val ssFirebaseAuth = FirebaseAuth.getInstance()
        val ssUser = ssFirebaseAuth.currentUser

        val params = Bundle()
        ssUser?.displayName?.let {
            params.putString(SSConstants.SS_EVENT_PARAM_USER_ID, ssUser.uid)
            params.putString(SSConstants.SS_EVENT_PARAM_USER_NAME, ssUser.displayName)
        } ?: params.putString(SSConstants.SS_EVENT_PARAM_USER_NAME, "Anonymous")

        values.forEach { (key, value) ->
            if (value is Int) {
                params.putInt(key, value)
            } else if (value is String) {
                params.putString(key, value)
            }
        }

        FirebaseAnalytics.getInstance(context)
                .logEvent(event_name, params)
    }
}