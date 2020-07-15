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

import android.content.SharedPreferences
import com.cryart.sabbathschool.data.model.Language
import com.cryart.sabbathschool.data.model.response.Resource
import com.cryart.sabbathschool.misc.SSConstants
import com.cryart.sabbathschool.model.SSQuarterly
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class QuarterliesRepository(private val firebaseDatabase: FirebaseDatabase,
                            private val preferences: SharedPreferences) {

    suspend fun getLanguages(): Resource<List<Language>> {
        // Switch to API when we migrate
        return getLanguagesFirebase()
    }

    private suspend fun getLanguagesFirebase(): Resource<List<Language>> {
        return suspendCoroutine { continuation ->
            firebaseDatabase.getReference(SSConstants.SS_FIREBASE_LANGUAGES_DATABASE)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Resource.error(error.toException()))
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val languages = snapshot.children.mapNotNull {
                                it.getValue(Language::class.java)
                            }
                            continuation.resume(Resource.success(languages))
                        }
                    })
        }
    }

    @ExperimentalCoroutinesApi
    fun getQuarterlies(languageCode: String? = null) = callbackFlow<Resource<List<SSQuarterly>>> {
        var code: String
        if (languageCode == null) {
            code = preferences.getString(SSConstants.SS_LAST_LANGUAGE_INDEX, Locale.getDefault().language)!!
            if (code == "iw") {
                code = "he"
            }
            if (code == "fil") {
                code = "tl"
            }
        } else {
            code = languageCode
        }

        val quarterliesRef = firebaseDatabase.getReference(
                SSConstants.SS_FIREBASE_QUARTERLIES_DATABASE)
                .child(code)

        val valueEventListener = quarterliesRef.addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        this@callbackFlow.close(error.toException())
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val quarterlies = snapshot.children.mapNotNull {
                            it.getValue(SSQuarterly::class.java)
                        }
                        this@callbackFlow.sendBlocking(Resource.success(quarterlies))
                    }
                })

        awaitClose {
            quarterliesRef.removeEventListener(valueEventListener)
        }
    }.flowOn(Dispatchers.IO)
            .catch {
                emit(Resource.error(it))
            }
}