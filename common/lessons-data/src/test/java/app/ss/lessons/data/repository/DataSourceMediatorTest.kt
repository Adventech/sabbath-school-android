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

import app.cash.turbine.test
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.response.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.test.TestDispatcherProvider

class DataSourceMediatorTest {

    private val cacheSource: LocalDataSource<Any, Any> = mockk()
    private val networkSource: DataSource<Any, Any> = mockk()

    private val dispatcherProvider: DispatcherProvider = TestDispatcherProvider()
    private val connectivityHelper: ConnectivityHelper = mockk()

    private lateinit var mediator: DataSourceMediator<Any, Any>

    @Before
    fun setup() {
        every { connectivityHelper.isConnected() }.returns(true)

        mediator = object : DataSourceMediator<Any, Any>(dispatcherProvider, connectivityHelper) {
            override val cache: LocalDataSource<Any, Any>
                get() = cacheSource
            override val network: DataSource<Any, Any>
                get() = networkSource
        }
    }

    @Test
    fun `should return cache data when network fails`() = runTest {
        val cacheResource = Resource.success(listOf("1", "2", "3"))
        val request = Any()

        coEvery { networkSource.get(request) }.returns(Resource.error(Throwable("Error")))
        coEvery { cacheSource.get(request) }.returns(cacheResource)

        val resource = mediator.get(request)

        resource.shouldBeEqualTo(cacheResource)
    }

    @Test
    fun `should cache network data when successful`() = runTest {
        val data = listOf("1", "2", "3")
        val networkResource = Resource.success(data)
        val request = Any()

        coEvery { networkSource.get(request) }.returns(networkResource)
        coEvery { cacheSource.update(data) }.returns(Unit)
        coEvery { cacheSource.update(request, data) }.returns(Unit)

        val resource = mediator.get(request)

        resource.shouldBeEqualTo(networkResource)
        coVerify { cacheSource.update(data) }
    }

    @Test
    fun `should not cache empty data`() = runTest {
        val data = emptyList<String>()
        val networkResource = Resource.success(data)
        val request = Any()

        coEvery { networkSource.get(request) }.returns(networkResource)

        val resource = mediator.get(request)

        resource.shouldBeEqualTo(networkResource)
        coVerify(inverse = true) {
            cacheSource.update(data)
            cacheSource.update(request, data)
        }
    }

    @Test
    fun `should emit cached resource then network resource when completed`() = runTest {
        val data = listOf("3", "4", "5")
        val cacheResource = Resource.success(listOf("1", "2"))
        val networkResource = Resource.success(data)
        val request = Any()

        coEvery { cacheSource.get(request) }.returns(cacheResource)
        coEvery { networkSource.get(request) }.returns(networkResource)
        coEvery { cacheSource.update(data) }.returns(Unit)

        mediator.getAsFlow(request).test {
            awaitItem() shouldBeEqualTo cacheResource
            awaitItem() shouldBeEqualTo networkResource
            awaitComplete()

            coVerify { cacheSource.update(data) }
        }
    }
}
