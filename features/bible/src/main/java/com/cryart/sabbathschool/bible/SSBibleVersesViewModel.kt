/*
 * Copyright (c) 2016 Adventech <info@adventech.io>
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
package com.cryart.sabbathschool.bible

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.core.extensions.arch.asLiveData
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import app.ss.lessons.data.model.SSBibleVerses
import app.ss.lessons.data.model.SSRead
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SSBibleVersesViewModel @Inject constructor(
    private val preferences: SSPrefs,
    database: FirebaseDatabase
) : ViewModel() {
    private val reference = database.reference.apply { keepSynced(true) }

    private val _verses = MutableLiveData<List<SSBibleVerses>>()
    val verses = _verses.asLiveData()

    fun getLastBibleUsed() = preferences.getLastBibleUsed()

    fun setLastBibleUsed(bibleId: String) = preferences.setLastBibleUsed(bibleId)

    fun getDisplayOptions() = preferences.getDisplayOptions()

    fun requestVerses(readIndex: String) = viewModelScope.launch {
        reference.child(SSConstants.SS_FIREBASE_READS_DATABASE)
            .child(readIndex)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val ssRead = SSRead(dataSnapshot)
                    _verses.postValue(ssRead.bible)
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                }
            })
    }
}
