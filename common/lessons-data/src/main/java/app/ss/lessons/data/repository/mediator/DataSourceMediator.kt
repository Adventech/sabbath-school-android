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

package app.ss.lessons.data.repository.mediator

import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber

internal abstract class DataSourceMediator<T>(
    private val dispatcherProvider: DispatcherProvider
) {

    abstract val cache: LocalDataSource<T>

    abstract val network: DataSource<T>

    suspend fun get(): Resource<List<T>> {
        val network = withContext(dispatcherProvider.default) { network.get() }

        return if (network.isSuccessFul) {
            network.also {
                it.data?.let {
                    withContext(dispatcherProvider.io) { cache.update(it) }
                }
            }
        } else {
            cache.get()
        }
    }

    fun getAsFlow(): Flow<Resource<List<T>>> = flow {
        val cacheResource = cache.get()
        if (cacheResource.isSuccessFul) {
            emit(cacheResource)
        }

        val network = withContext(dispatcherProvider.default) { network.get() }
        if (network.isSuccessFul) {
            val data = network.data ?: emptyList()
            cache.update(data)

            emit(network)
        }
    }
        .flowOn(dispatcherProvider.io)
        .catch {
            Timber.e(it)
            emit(Resource.error(it))
        }
}

interface DataSource<T> {
    suspend fun get(): Resource<List<T>>
}

interface LocalDataSource<T> : DataSource<T> {
    fun update(data: List<T>)
}
