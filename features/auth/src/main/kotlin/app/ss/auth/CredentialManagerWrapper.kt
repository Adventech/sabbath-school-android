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

package app.ss.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** A wrapper around [CredentialManager] for easier testing. */
interface CredentialManagerWrapper {
    suspend fun getCredential(
        context: Context,
        request: GetCredentialRequest,
    ): GetCredentialResponse
}

internal class CredentialManagerWrapperImpl @Inject constructor(
    @ApplicationContext context: Context,
) : CredentialManagerWrapper {
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    override suspend fun getCredential(context: Context, request: GetCredentialRequest): GetCredentialResponse {
        return credentialManager.getCredential(context, request)
    }
}
