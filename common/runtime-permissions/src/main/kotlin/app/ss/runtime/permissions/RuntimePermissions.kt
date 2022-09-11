/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.runtime.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface RuntimePermissions {
    fun isGranted(permission: String): Boolean
    fun setup(caller: ActivityResultCaller, listener: Listener)
    fun request(permission: String)

    interface Listener {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }
}

internal class RuntimePermissionsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RuntimePermissions {

    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    override fun isGranted(permission: String): Boolean = ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED

    override fun setup(caller: ActivityResultCaller, listener: RuntimePermissions.Listener) {
        requestPermissionLauncher =
            caller.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    listener.onPermissionGranted()
                } else {
                    listener.onPermissionDenied()
                }
            }
    }

    override fun request(permission: String) {
        if (requestPermissionLauncher == null) throw IllegalStateException("Call setup before requesting permissions")

        requestPermissionLauncher?.launch(permission)
    }
}
