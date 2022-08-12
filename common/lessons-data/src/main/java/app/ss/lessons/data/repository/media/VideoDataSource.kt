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
import app.ss.lessons.data.model.api.VideosInfoModel
import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.media.SSVideosInfo
import app.ss.storage.db.dao.VideoInfoDao
import app.ss.storage.db.entity.VideoInfoEntity
import com.cryart.sabbathschool.core.extensions.connectivity.ConnectivityHelper
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class VideoDataSource @Inject constructor(
    private val mediaApi: SSMediaApi,
    private val videoInfoDao: VideoInfoDao,
    dispatcherProvider: DispatcherProvider,
    connectivityHelper: ConnectivityHelper
) : DataSourceMediator<SSVideosInfo, VideoDataSource.Request>(
    dispatcherProvider = dispatcherProvider,
    connectivityHelper = connectivityHelper
) {

    data class Request(val lessonIndex: String)

    override val cache: LocalDataSource<SSVideosInfo, Request> = object : LocalDataSource<SSVideosInfo, Request> {
        override suspend fun get(request: Request): Resource<List<SSVideosInfo>> {
            val data = request.lessonIndex.toMediaRequest()?.let { ssMediaRequest ->
                videoInfoDao.get("${ssMediaRequest.language}-${ssMediaRequest.quarterlyId}")
            } ?: emptyList()
            return if (data.isNotEmpty()) Resource.success(data.map { it.toModel() }) else Resource.loading()
        }

        override suspend fun update(data: List<SSVideosInfo>) {
            videoInfoDao.insertAll(data.map { it.toEntity() })
        }
    }

    override val network: DataSource<SSVideosInfo, Request> = object : DataSource<SSVideosInfo, Request> {
        override suspend fun get(request: Request): Resource<List<SSVideosInfo>> {
            val data = request.lessonIndex.toMediaRequest()?.let { ssMediaRequest ->
                val lessonIndex = "${ssMediaRequest.language}-${ssMediaRequest.quarterlyId}"
                val videos = mediaApi.getVideo(ssMediaRequest.language, ssMediaRequest.quarterlyId).body()
                videos?.mapIndexed { index, model ->
                    model.toModel("$lessonIndex-$index", lessonIndex)
                }
            } ?: emptyList()

            return Resource.success(data)
        }
    }
}

private fun VideoInfoEntity.toModel(): SSVideosInfo = SSVideosInfo(
    id = id,
    artist = artist,
    clips = clips,
    lessonIndex = lessonIndex
)

private fun VideosInfoModel.toModel(
    id: String,
    lessonIndex: String
): SSVideosInfo = SSVideosInfo(
    id = id,
    artist = artist,
    clips = clips,
    lessonIndex = lessonIndex
)

private fun SSVideosInfo.toEntity(): VideoInfoEntity = VideoInfoEntity(
    id = id,
    artist = artist,
    clips = clips,
    lessonIndex = lessonIndex
)
