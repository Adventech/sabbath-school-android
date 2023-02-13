/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.account

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.auth.AuthRepository
import app.ss.lessons.data.repository.user.UserDataRepository
import com.cryart.sabbathschool.account.model.UserInfo
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    val userInfoFlow: StateFlow<UserInfo> = authRepository
        .getUserFlow()
        .map { user ->
            user?.let {
                UserInfo(
                    user.displayName,
                    user.email,
                    user.photo?.toUri()
                )
            } ?: UserInfo()
        }
        .stateIn(viewModelScope, UserInfo())

    fun logoutClicked() {
        viewModelScope.launch {
            userDataRepository.clear()
            authRepository.logout()
        }
    }
}
