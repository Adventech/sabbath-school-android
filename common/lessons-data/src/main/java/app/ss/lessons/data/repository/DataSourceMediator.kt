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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.lessons.data.repository

import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import timber.log.Timber

internal abstract class DataSourceMediator<T, R>(
    private val dispatcherProvider: DispatcherProvider,
    private val connectivityHelper: ConnectivityHelper
) : Scopable by ioScopable(dispatcherProvider) {

    abstract val cache: LocalDataSource<T, R>

    abstract val network: DataSource<T, R>

    suspend fun get(request: R): Resource<List<T>> = withContext(dispatcherProvider.io) {
        val networkResource = safeNetworkGet { network.get(request) }

        if (networkResource.isSuccessFul) {
            networkResource.also { resource ->
                resource.data?.takeIf { it.isNotEmpty() }?.let {
                    cache.update(it)
                    cache.update(request, it)
                }
            }
        } else {
            cache.get(request)
        }
    }

    fun getAsFlow(request: R): Flow<Resource<List<T>>> = flow {
        val cacheResource = cache.get(request)
        if (cacheResource.isSuccessFul) {
            emit(cacheResource)
        }

        val network = safeNetworkGet { network.get(request) }
        if (network.isSuccessFul) {
            network.data?.let { cache.update(it) }

            emit(network)
        }
    }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Resource.error(it))
        }

    suspend fun getItem(request: R): Resource<T> = withContext(dispatcherProvider.io) {
        cache.getItem(request)
            .takeIf { it.isSuccessFul } ?: safeNetworkGetItem { network.getItem(request) }
            .also { cacheNetworkRequest(request) }
    }

    private fun cacheNetworkRequest(request: R) = scope.launch {
        val resource = safeNetworkGetItem { network.getItem(request) }
        resource.data?.let { withContext(dispatcherProvider.io) { cache.updateItem(it) } }
    }

    fun getItemAsFlow(request: R): Flow<Resource<T>> = flow {
        val cacheResource = cache.getItem(request)
        if (cacheResource.isSuccessFul) {
            emit(cacheResource)
        }

        val network = safeNetworkGetItem { network.getItem(request) }
        if (network.isSuccessFul) {
            network.data?.let { cache.updateItem(it) }

            emit(network)
        }
    }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Resource.error(it))
        }

    private suspend fun safeNetworkGetItem(call: suspend () -> Resource<T>): Resource<T> {
        return when (val resource = safeApiCall(connectivityHelper) { call() }) {
            is NetworkResource.Failure -> Resource.error(Throwable())
            is NetworkResource.Success -> resource.value
        }
    }

    private suspend fun safeNetworkGet(call: suspend () -> Resource<List<T>>): Resource<List<T>> {
        return when (val resource = safeApiCall(connectivityHelper) { call() }) {
            is NetworkResource.Failure -> Resource.error(Throwable())
            is NetworkResource.Success -> resource.value
        }
    }
}

interface DataSource<T, R> {
    suspend fun get(request: R): Resource<List<T>> = Resource.success(emptyList())
    suspend fun getItem(request: R): Resource<T> = Resource.loading()
    suspend fun update(request: R, data: List<T>) = Unit
}

interface LocalDataSource<T, R> : DataSource<T, R> {
    suspend fun update(data: List<T>) = Unit
    suspend fun updateItem(data: T) = Unit
}
