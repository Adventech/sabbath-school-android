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

import app.ss.auth.api.AuthRequest
import app.ss.auth.api.SSAuthApi
import app.ss.auth.api.UserModel
import app.ss.auth.api.toEntity
import app.ss.auth.api.toModel
import app.ss.models.auth.SSUser
import app.ss.network.safeApiCall
import app.ss.storage.db.dao.UserDao
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {

    /**
     * Get the signed in user
     */
    suspend fun getUser(): Resource<SSUser?>

    /**
     * Get the signed in user
     */
    fun getUserFlow(): Flow<SSUser?>

    /**
     * Sign in anonymously
     */
    suspend fun signIn(): Resource<AuthResponse>

    /**
     * Sign in with a provider [token]
     */
    suspend fun signIn(token: String): Resource<AuthResponse>

    /**
     * Sign out
     */
    suspend fun logout()

    /**
     * Deletes the user account and then calls [logout]
     */
    suspend fun deleteAccount()
}

@Singleton
internal class AuthRepositoryImpl @Inject constructor(
    private val authApi: SSAuthApi,
    private val userDao: UserDao,
    private val dispatcherProvider: DispatcherProvider,
    private val connectivityHelper: ConnectivityHelper
) : AuthRepository {

    override suspend fun getUser(): Resource<SSUser?> = withContext(dispatcherProvider.io) {
        val user = userDao.get()
        Resource.success(user?.toModel())
    }

    override fun getUserFlow(): Flow<SSUser?> = userDao
        .getAsFlow()
        .map { it?.toModel() }
        .flowOn(dispatcherProvider.io)

    override suspend fun signIn(): Resource<AuthResponse> = makeAuthRequest { authApi.signIn() }

    override suspend fun signIn(token: String): Resource<AuthResponse> = makeAuthRequest { authApi.signIn(AuthRequest(token)) }

    private suspend fun makeAuthRequest(request: suspend () -> Response<UserModel>): Resource<AuthResponse> = try {
        val response = withContext(dispatcherProvider.default) { request() }
        val user = response.body()
        val result = if (user != null) {
            cacheUser(user)
            AuthResponse.Authenticated(user.toModel())
        } else {
            AuthResponse.Error
        }
        Resource.success(result)
    } catch (error: Throwable) {
        Timber.e(error)
        Resource.error(error)
    }

    private suspend fun cacheUser(user: UserModel) = withContext(dispatcherProvider.io) {
        userDao.clear()
        userDao.insertItem(user.toEntity())
    }

    override suspend fun logout() = withContext(dispatcherProvider.io) { userDao.clear() }

    override suspend fun deleteAccount() {
        withContext(dispatcherProvider.default) {
            safeApiCall(connectivityHelper) {
                if (authApi.deleteAccount().isSuccessful) {
                    logout()
                }
            }
        }
    }
}
