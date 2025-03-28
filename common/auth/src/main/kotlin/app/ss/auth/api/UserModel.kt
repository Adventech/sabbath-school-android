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

package app.ss.auth.api

import androidx.annotation.Keep
import app.ss.models.auth.AccountToken
import app.ss.models.auth.SSUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ss.libraries.storage.api.entity.UserEntity

@Keep
@JsonClass(generateAdapter = true)
data class UserModel(
    val uid: String,
    val displayName: String?,
    val email: String?,
    @param:Json(name = "photoURL") val photo: String?,
    val emailVerified: Boolean,
    val phoneNumber: String?,
    val isAnonymous: Boolean,
    val tenantId: String?,
    val stsTokenManager: AccountToken
)

internal fun UserEntity.toModel(): SSUser = SSUser(
    uid = uid,
    displayName = displayName,
    email = email,
    stsTokenManager = stsTokenManager,
    photo = photo,
    emailVerified = emailVerified,
    phoneNumber = phoneNumber,
    isAnonymous = isAnonymous,
    tenantId = tenantId
)

internal fun UserEntity.toUserModel(): UserModel = UserModel(
    uid = uid,
    displayName = displayName,
    email = email,
    stsTokenManager = stsTokenManager,
    photo = photo,
    emailVerified = emailVerified,
    phoneNumber = phoneNumber,
    isAnonymous = isAnonymous,
    tenantId = tenantId
)

internal fun UserModel.toModel(): SSUser = SSUser(
    uid = uid,
    displayName = displayName,
    email = email,
    stsTokenManager = stsTokenManager,
    photo = photo,
    emailVerified = emailVerified,
    phoneNumber = phoneNumber,
    isAnonymous = isAnonymous,
    tenantId = tenantId
)

internal fun UserModel.toEntity(): UserEntity = UserEntity(
    uid = uid,
    displayName = displayName,
    email = email,
    stsTokenManager = stsTokenManager,
    photo = photo,
    emailVerified = emailVerified,
    phoneNumber = phoneNumber,
    isAnonymous = isAnonymous,
    tenantId = tenantId
)
