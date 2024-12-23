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

package ss.lessons.api

import app.ss.models.feed.FeedGroup
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import app.ss.models.resource.FeedResponse
import app.ss.models.resource.LanguageResponse
import app.ss.models.resource.Resource
import app.ss.models.resource.ResourceDocument

interface ResourcesApi {

    @GET("api/v3/resources/index.json")
    suspend fun languages(): Response<List<LanguageResponse>>

    @GET("api/v3/{language}/{type}/index.json")
    suspend fun feed(@Path("language") language: String, @Path("type") type: String): Response<FeedResponse>

    @GET("api/v3/{language}/{type}/feeds/{groupId}/index.json")
    suspend fun feedGroup(
        @Path("language") language: String,
        @Path("type") type: String,
        @Path("groupId") groupId: String,
    ): Response<FeedGroup>

    @GET("api/v3/{index}/sections/index.json")
    suspend fun resource(@Path("index") index: String): Response<Resource>

    @GET("api/v3/{index}/index.json")
    suspend fun document(@Path("index") index: String): Response<ResourceDocument>
}
