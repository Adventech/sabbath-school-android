/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.lessons.data.repository.media

import app.ss.lessons.data.api.SSMediaApi
import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.media.SSAudio
import app.ss.storage.db.dao.AudioDao
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AudioDataSource @Inject constructor(
    connectivityHelper: ConnectivityHelper,
    private val mediaApi: SSMediaApi,
    private val audioDao: AudioDao,
    private val dispatcherProvider: DispatcherProvider
) : DataSourceMediator<SSAudio, AudioDataSource.Request>(
    dispatcherProvider = dispatcherProvider,
    connectivityHelper = connectivityHelper
) {

    data class Request(val lessonIndex: String)

    override val cache: LocalDataSource<SSAudio, Request> = object : LocalDataSource<SSAudio, Request> {
        override suspend fun get(request: Request): Resource<List<SSAudio>> {
            val data = audioDao.get().filter { it.targetIndex.startsWith(request.lessonIndex) }

            return if (data.isNotEmpty()) Resource.success(data.map { it.toSSAudio() }) else Resource.loading()
        }

        override suspend fun update(data: List<SSAudio>) {
            audioDao.insertAll(data.map { it.toEntity() })
        }
    }

    override val network: DataSource<SSAudio, Request> = object : DataSource<SSAudio, Request> {
        override suspend fun get(request: Request): Resource<List<SSAudio>> {
            val data = request.lessonIndex.toMediaRequest()?.let {
                mediaApi.getAudio(it.language, it.quarterlyId).body()
            } ?: emptyList()

            withContext(dispatcherProvider.io) { audioDao.insertAll(data.map { it.toEntity() }) }

            val lessonAudios = data.filter { it.targetIndex.startsWith(request.lessonIndex) }

            return Resource.success(lessonAudios)
        }
    }
}
