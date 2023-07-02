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

package app.ss.auth

import app.ss.auth.api.SSAuthApi
import app.ss.storage.db.dao.UserDao
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import ss.foundation.coroutines.test.TestDispatcherProvider

class AuthRepositoryTest {

    private val mockAuthApi: SSAuthApi = mockk()
    private val mockUserDao: UserDao = mockk()
    private val mockConnectivityHelper: ConnectivityHelper = mockk()

    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        coEvery { mockUserDao.clear() }.returns(Unit)
        every { mockConnectivityHelper.isConnected() }.returns(true)

        repository = AuthRepositoryImpl(
            authApi = mockAuthApi,
            userDao = mockUserDao,
            dispatcherProvider = TestDispatcherProvider(),
            connectivityHelper = mockConnectivityHelper
        )
    }

    @Test
    fun deleteAccount() = runTest {
        coEvery { mockAuthApi.deleteAccount() }.returns(Response.success(mockk()))

        repository.deleteAccount()

        coVerify { mockUserDao.clear() }
    }

    @Test
    fun deleteAccountFail() = runTest {
        val mockResponseBody = mockk<ResponseBody>()
        every { mockResponseBody.contentType() }.returns(null)
        every { mockResponseBody.contentLength() }.returns(0L)
        coEvery { mockAuthApi.deleteAccount() }.returns(Response.error(500, mockResponseBody))

        repository.deleteAccount()

        coVerify(inverse = true) { mockUserDao.clear() }
    }

}
