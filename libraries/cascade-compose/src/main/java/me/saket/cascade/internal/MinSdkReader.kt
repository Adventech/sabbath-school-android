/*
 * Copyright 2020 Saket Narayan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.saket.cascade.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal fun interface MinSdkReader {
    @Composable
    fun minSdk(): Int
}

internal object RealMinSdkReader : MinSdkReader {
    @Composable
    override fun minSdk(): Int {
        val context = LocalContext.current
        return remember {
            context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo?.minSdkVersion ?: 0
        }
    }
}
