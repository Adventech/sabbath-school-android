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

package ss.navigation.suite.data

import app.ss.models.LanguageResource
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import jakarta.inject.Inject
import kotlinx.coroutines.withContext
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.SSResourcesApi
import timber.log.Timber

interface ResourcesRepository {
    suspend fun get(): List<LanguageResource>
}

class ResourcesRepositoryImpl @Inject constructor(
    private val resourcesApi: SSResourcesApi,
    private val dispatcherProvider: DispatcherProvider,
    private val connectivityHelper: ConnectivityHelper,
) : ResourcesRepository {

    override suspend fun get(): List<LanguageResource> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) { resourcesApi.get() }) {
                is NetworkResource.Failure -> {
                    Timber.e("Failed to fetch resources, ${resource.errorBody}")
                    emptyList()
                }
                is NetworkResource.Success -> {
                    resource.value.body() ?: emptyList()
                }
            }
        }
    }
}
