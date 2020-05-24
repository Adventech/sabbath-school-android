/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.ui.account

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cryart.sabbathschool.extensions.arch.asLiveData
import com.cryart.sabbathschool.misc.SSConstants
import com.cryart.sabbathschool.model.UserInfo
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AccountViewModel @Inject constructor(private val prefs: SharedPreferences,
                                           private val firebaseAuth: FirebaseAuth) : ViewModel() {

    private val mutableUserInfo = MutableLiveData<UserInfo>()
    val userInfoLiveData: LiveData<UserInfo> = mutableUserInfo.asLiveData()

    init {
        val name = prefs.getString(SSConstants.SS_USER_NAME_INDEX, null)
        val email = prefs.getString(SSConstants.SS_USER_EMAIL_INDEX, null)
        val photo = prefs.getString(SSConstants.SS_USER_PHOTO_INDEX, null)

        val user = UserInfo(name, email, photo)
        mutableUserInfo.postValue(user)
    }

    fun logoutClicked() {
        prefs.edit { clear() }
        firebaseAuth.signOut()
    }
}