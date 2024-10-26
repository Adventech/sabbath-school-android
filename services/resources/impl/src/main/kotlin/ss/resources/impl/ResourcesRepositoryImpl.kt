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

package ss.resources.impl

import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import kotlinx.coroutines.withContext
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.prefs.api.SSPrefs
import ss.lessons.api.ResourcesApi
import ss.resources.api.ResourcesRepository
import ss.resources.model.FeedModel
import ss.resources.model.LanguageModel
import javax.inject.Inject
import dagger.Lazy
import ss.resources.model.FeedType

internal class ResourcesRepositoryImpl @Inject constructor(
    private val resourcesApi: ResourcesApi,
    private val dispatcherProvider: DispatcherProvider,
    private val connectivityHelper: ConnectivityHelper,
    private val ssPrefs: Lazy<SSPrefs>,
) : ResourcesRepository {

    override suspend fun languages(): Result<List<LanguageModel>> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) { resourcesApi.languages() }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch languages, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    val languages = resource.value.body()?.map {
                        LanguageModel(
                            name = it.name,
                            code = it.code,
                            devo = it.devo,
                            pm = it.pm,
                            aij = it.aij,
                            ss = it.ss,
                        )
                    } ?: emptyList()

                    Result.success(languages)
                }
            }
        }
    }

    override suspend fun feed(type: FeedType): Result<FeedModel> {
        return withContext(dispatcherProvider.default) {
            when (val resource = safeApiCall(connectivityHelper) {
                resourcesApi.feed(
                    language = ssPrefs.get().getLanguageCode(),
                    type = type.toPath()
                )
            }) {
                is NetworkResource.Failure -> {
                    Result.failure(Throwable("Failed to fetch feed, ${resource.errorBody}"))
                }

                is NetworkResource.Success -> {
                    resource.value.body()?.let {
                        Result.success(
                            FeedModel(title = it.title)
                        )
                    } ?: Result.failure(Throwable("Failed to fetch feed, body is null"))
                }
            }
        }
    }

    private fun FeedType.toPath(): String {
        return when (this) {
            FeedType.ALIVE_IN_JESUS -> "aij"
            FeedType.PERSONAL_MINISTRIES -> "pm"
            FeedType.DEVOTIONALS -> "devo"
        }
    }
}
