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

package app.ss.lessons.data.repository.quarterly

import app.ss.lessons.data.extensions.ValueEvent
import app.ss.lessons.data.extensions.singleEvent
import app.ss.lessons.data.model.Language
import app.ss.models.SSQuarterlyInfo
import app.ss.lessons.data.repository.mediator.LanguagesDataSource
import app.ss.lessons.data.repository.mediator.QuarterliesDataSource
import app.ss.lessons.data.repository.mediator.QuarterliesDataSource.Companion.LANGUAGE
import app.ss.lessons.data.repository.mediator.QuarterliesDataSource.Companion.QUARTERLY_GROUP
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class QuarterliesRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val ssPrefs: SSPrefs,
    private val languagesSource: LanguagesDataSource,
    private val quarterliesDataSource: QuarterliesDataSource,
) : QuarterliesRepository {

    override suspend fun getLanguages(): Resource<List<Language>> = languagesSource.get()

    override suspend fun getQuarterlies(
        languageCode: String?,
        group: QuarterlyGroup?
    ): Flow<Resource<List<SSQuarterly>>> {
        val code = languageCode ?: ssPrefs.getLanguageCode()
        val params = mapOf(
            LANGUAGE to code,
            QUARTERLY_GROUP to group
        ).mapNotNull { map -> map.value?.let { map.key to it } }

        return quarterliesDataSource.getAsFlow(params.toMap())
    }

    override suspend fun getQuarterlyInfo(index: String): Resource<SSQuarterlyInfo> {
        val event = firebaseDatabase.reference
            .child(SSConstants.SS_FIREBASE_QUARTERLY_INFO_DATABASE)
            .child(index)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> Resource.error(event.error)
            is ValueEvent.DataChange -> Resource.error(Throwable(""))
        }
    }
}
