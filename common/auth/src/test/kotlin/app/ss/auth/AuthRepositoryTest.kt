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

package app.ss.auth

import app.cash.turbine.test
import app.ss.auth.api.SSAuthApi
import app.ss.models.auth.AccountToken
import app.ss.storage.test.FakeUserDao
import app.ss.storage.test.FakeUserInputDao
import io.adventech.blockkit.model.input.UserInput
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotContain
import org.junit.Test
import retrofit2.Response
import ss.foundation.android.connectivity.FakeConnectivityHelper
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.libraries.storage.api.entity.UserEntity
import ss.libraries.storage.api.entity.UserInputEntity

class AuthRepositoryTest {

    private val mockAuthApi: SSAuthApi = mockk()
    private val fakeUserDao = FakeUserDao()
    private val fakeUserInputDao = FakeUserInputDao()
    private val fakeConnectivityHelper = FakeConnectivityHelper(true)

    private val fakeUser = UserEntity(
        uid = "uid",
        displayName = "displayName",
        email = "email",
        photo = "photo",
        emailVerified = true,
        phoneNumber = "phoneNumber",
        isAnonymous = false,
        tenantId = "tenantId",
        stsTokenManager = AccountToken.fake(),
    )

    private val repository: AuthRepository = AuthRepositoryImpl(
        authApi = mockAuthApi,
        userDao = fakeUserDao,
        userInputDao = fakeUserInputDao,
        dispatcherProvider = TestDispatcherProvider(),
        connectivityHelper = fakeConnectivityHelper,
    )

    @Test
    fun deleteAccount() = runTest {
        fakeUserDao.insertItem(fakeUser)
        fakeUserDao.getCurrent() shouldBe fakeUser

        coEvery { mockAuthApi.deleteAccount() }.returns(Response.success(mockk()))

        repository.deleteAccount()

        fakeUserDao.getCurrent() shouldBe null
    }

    @Test
    fun deleteAccountFail() = runTest {
        fakeUserDao.insertItem(fakeUser)
        fakeUserDao.getCurrent() shouldBe fakeUser

        val mockResponseBody = mockk<ResponseBody>()
        every { mockResponseBody.contentType() }.returns(null)
        every { mockResponseBody.contentLength() }.returns(0L)
        coEvery { mockAuthApi.deleteAccount() }.returns(Response.error(500, mockResponseBody))

        repository.deleteAccount()

        fakeUserDao.getCurrent() shouldBe fakeUser
    }

    @Test
    fun `logout - should clear logged in user`() = runTest {
        fakeUserDao.currentUserFlow.test {
            awaitItem() shouldBe null

            fakeUserDao.insertItem(fakeUser)
            awaitItem() shouldBe fakeUser

            repository.logout()

            awaitItem() shouldBe null

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `logout - should clear all user input`() = runTest {
        val userInput = UserInputEntity(
            localId = "localId",
            id = "id",
            documentId = "documentId",
            input = UserInput.Unknown,
            timestamp = 0L,
        )

        fakeUserInputDao.insertItem(userInput)
        fakeUserInputDao.items shouldContain userInput

        repository.logout()

        fakeUserInputDao.items shouldNotContain userInput
    }

}
