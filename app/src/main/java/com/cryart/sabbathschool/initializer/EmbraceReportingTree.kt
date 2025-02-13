/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.initializer

import android.util.Log
import io.embrace.android.embracesdk.Embrace
import io.embrace.android.embracesdk.Severity
import timber.log.Timber

class EmbraceReportingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val properties = mutableMapOf<String, Any>().apply {
            tag?.let { put("tag", it) }
        }

        val severity = when (priority) {
            Log.ERROR -> Severity.ERROR
            Log.WARN -> Severity.WARNING
            else -> Severity.INFO
        }

        if (t != null) {
            // Log exceptions with severity and properties
            Embrace.getInstance().logException(t, severity, properties)
        } else {
            // Log regular messages with severity and properties
            Embrace.getInstance().logMessage(message, severity, properties)
        }
    }
}
