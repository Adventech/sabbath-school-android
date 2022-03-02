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

package com.cryart.sabbathschool.core.extensions.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface ConnectivityHelper {
    fun isConnected(): Boolean
}

@Singleton
internal class ConnectivityHelperImpl @Inject constructor(
    @ApplicationContext context: Context
) : ConnectivityHelper {

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun isConnected(): Boolean = connectivityManager.isConnected
}

val ConnectivityManager.isConnected: Boolean
    get() = isConnectedToWifi || isConnectedToCellular

val ConnectivityManager.isConnectedToWifi: Boolean
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            val info = getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            info?.isConnected ?: false
        }
    }

val ConnectivityManager.isConnectedToCellular: Boolean
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val info = getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            info?.isConnected ?: false
        }
    }
