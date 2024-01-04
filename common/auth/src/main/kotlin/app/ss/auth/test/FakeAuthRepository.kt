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

package app.ss.auth.test

import androidx.annotation.VisibleForTesting
import app.ss.auth.AuthRepository
import app.ss.auth.AuthResponse
import app.ss.models.auth.SSUser
import kotlinx.coroutines.flow.Flow

/**
 * Fake implementation of [AuthRepository] for use in tests.
 */
@VisibleForTesting
class FakeAuthRepository : AuthRepository {

    var userDelegate: () -> Result<SSUser?> = { throw NotImplementedError() }

    override suspend fun getUser(): Result<SSUser?> = userDelegate()

    override fun getUserFlow(): Flow<SSUser?> {
        TODO("Not yet implemented")
    }

    override suspend fun signIn(): Result<AuthResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun signIn(token: String): Result<AuthResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAccount() {
        TODO("Not yet implemented")
    }
}
