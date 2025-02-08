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

package ss.resources.impl.sync

import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import io.adventech.blockkit.model.input.UserInputRequest
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.defaultScopable
import ss.lessons.api.ResourcesApi
import ss.libraries.storage.api.dao.DocumentsDao
import ss.libraries.storage.api.dao.LanguagesDao
import ss.libraries.storage.api.dao.SegmentsDao
import ss.libraries.storage.api.dao.UserInputDao
import ss.libraries.storage.api.entity.LanguageEntity
import ss.libraries.storage.api.entity.UserInputEntity
import ss.resources.impl.ext.localId
import ss.resources.impl.ext.toEntity
import ss.resources.impl.ext.toInput
import ss.resources.impl.ext.type
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.map
import kotlin.collections.orEmpty

interface SyncHelper {
    fun syncLanguages()
    fun syncDocument(index: String)
    fun syncUserInput(documentId: String)
    fun saveUserInput(documentId: String, userInput: UserInputRequest)
    fun syncSegment(index: String)
}

internal class SyncHelperImpl @Inject constructor(
    private val resourcesApi: ResourcesApi,
    private val languagesDao: LanguagesDao,
    private val documentsDao: DocumentsDao,
    private val userInputDao: UserInputDao,
    private val segmentsDao: SegmentsDao,
    private val connectivityHelper: ConnectivityHelper,
    private val dispatcherProvider: DispatcherProvider
) : SyncHelper, Scopable by defaultScopable(dispatcherProvider) {

    private val exceptionLogger = CoroutineExceptionHandler { _, exception -> Timber.e(exception) }

    override fun syncLanguages() {
        scope.launch(exceptionLogger) {
            when (val response = safeApiCall(connectivityHelper) { resourcesApi.languages() }) {
                is NetworkResource.Failure -> Unit
                is NetworkResource.Success -> response.value.body()?.let { data ->
                    withContext(dispatcherProvider.io) {
                        languagesDao.insertAll(data.map {
                            LanguageEntity(
                                code = it.code,
                                name = it.name,
                                nativeName = getNativeLanguageName(it.code, it.name),
                                devo = it.devo,
                                pm = it.pm,
                                aij = it.aij,
                                ss = it.ss,
                                explore = it.explore,
                            )
                        })
                    }
                }
            }
        }
    }

    override fun syncDocument(index: String) {
        scope.launch(exceptionLogger) {
            when (val response = safeApiCall(connectivityHelper) { resourcesApi.document(index) }) {
                is NetworkResource.Failure -> {
                    Timber.e("Failed to fetch document for index: $index => ${response.throwable?.message}")
                }
                is NetworkResource.Success -> response.value.body()?.let { data ->
                    withContext(dispatcherProvider.io) {
                        documentsDao.insertItem(data.toEntity())
                        segmentsDao.insertAll(data.segments.orEmpty().map { it.toEntity() })
                    }
                }
            }
        }
    }

    override fun syncUserInput(documentId: String) {
        scope.launch(exceptionLogger) {
            when (val response = safeApiCall(connectivityHelper) { resourcesApi.userInput(documentId) }) {
                is NetworkResource.Failure -> {
                    Timber.e("Failed to fetch user input for documentId: $documentId => ${response.throwable?.message}")
                }
                is NetworkResource.Success -> response.value.body()?.let { data ->
                    withContext(dispatcherProvider.io) {
                        userInputDao.insertAll(data.map { input ->
                            // Compare timestamp with local timestamp here
                            UserInputEntity(
                                localId = input.localId(documentId),
                                id = input.id,
                                documentId = documentId,
                                input = input,
                                timestamp = input.timestamp,
                            )
                        })
                    }
                }
            }
        }
    }

    override fun saveUserInput(documentId: String, userInput: UserInputRequest) {
        scope.launch(exceptionLogger) {
            withContext(dispatcherProvider.io) {
                val localId = userInput.localId(documentId)
                val id = userInputDao.getId(localId)
                val timestamp = System.currentTimeMillis()

                userInputDao.insertItem(
                    UserInputEntity(
                        localId = localId,
                        id = id,
                        documentId = documentId,
                        input = userInput.toInput(id ?: localId, timestamp),
                        timestamp = timestamp,
                    )
                )
            }

            resourcesApi.saveUserInput(
                inputType = userInput.type(),
                documentId = documentId,
                blockId = userInput.blockId,
                userInput = userInput,
            )
        }
    }

    override fun syncSegment(index: String) {
        scope.launch(exceptionLogger) {
            when (val response = safeApiCall(connectivityHelper) { resourcesApi.segment(index) }) {
                is NetworkResource.Failure -> {
                    Timber.e("Failed to fetch segment for index: $index => ${response.throwable?.message}")
                }
                is NetworkResource.Success -> response.value.body()?.let { data ->
                    withContext(dispatcherProvider.io) {
                        segmentsDao.insertItem(data.toEntity())
                    }
                }
            }
        }
    }

    private fun getNativeLanguageName(languageCode: String, languageName: String): String {
        val loc = Locale(languageCode)
        val name = loc.getDisplayLanguage(loc).takeUnless { it == languageCode } ?: languageName
        return name.replaceFirstChar { it.uppercase() }
    }
}
