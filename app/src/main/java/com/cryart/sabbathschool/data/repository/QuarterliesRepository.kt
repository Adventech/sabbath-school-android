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

package com.cryart.sabbathschool.data.repository

import com.cryart.sabbathschool.data.api.SSApi
import com.cryart.sabbathschool.data.model.Language
import com.cryart.sabbathschool.data.model.response.Resource
import com.cryart.sabbathschool.misc.SSConstants
import com.cryart.sabbathschool.model.SSQuarterly
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class QuarterliesRepository(private val firebaseDatabase: FirebaseDatabase,
                            private val ssApi: SSApi) {

    suspend fun getLanguages(): Resource<List<Language>> {
        return try {
            val response = ssApi.listLanguages()
            if (response.isSuccessful && response.body() != null) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(Throwable())
            }
        } catch (ex: Exception) {
            Timber.e(ex)
            Resource.error(ex)
        }
    }

    suspend fun getQuarterlies(languageCode: String): Resource<List<SSQuarterly>> {
        return suspendCoroutine { continuation ->
            firebaseDatabase.getReference(SSConstants.SS_FIREBASE_QUARTERLIES_DATABASE)
                    .child(languageCode)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Resource.error(error.toException()))
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val quarterlies = snapshot.children.mapNotNull {
                                it.getValue(SSQuarterly::class.java)
                            }
                            continuation.resume(Resource.success(quarterlies))
                        }
                    })
        }
    }
}