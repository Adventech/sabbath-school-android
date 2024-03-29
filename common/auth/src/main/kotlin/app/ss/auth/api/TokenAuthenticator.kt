/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.auth.api

import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.libraries.storage.api.dao.UserDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val authApi: SSTokenApi,
    private val userDao: UserDao,
    private val dispatcherProvider: DispatcherProvider,
    private val connectivityHelper: ConnectivityHelper
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            val user = userDao.get() ?: return@runBlocking null

            when (val resource = getUpdatedToken(user.toUserModel())) {
                is NetworkResource.Success -> {
                    withContext(dispatcherProvider.io) {
                        userDao.clear()
                        userDao.insertItem(resource.value.toEntity())
                    }

                    response.request.newBuilder()
                        .header("x-ss-auth-access-token", resource.value.stsTokenManager.accessToken)
                        .build()
                }
                else -> null
            }
        }
    }

    private suspend fun getUpdatedToken(
        userModel: UserModel
    ): NetworkResource<UserModel> = withContext(dispatcherProvider.io) {
        safeApiCall(connectivityHelper) { authApi.refreshToken(userModel) }
    }
}
