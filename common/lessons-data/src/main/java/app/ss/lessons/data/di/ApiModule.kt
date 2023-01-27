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

package app.ss.lessons.data.di

import app.ss.auth.api.TokenAuthenticator
import app.ss.lessons.data.api.SSLessonsApi
import app.ss.lessons.data.api.SSMediaApi
import app.ss.lessons.data.api.SSQuarterliesApi
import app.ss.storage.db.dao.UserDao
import com.cryart.sabbathschool.core.model.AppConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ss.misc.SSConstants
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun retrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    private fun baseUrl(appConfig: AppConfig): String = if (appConfig.isDebug)
        SSConstants.SS_STAGE_API_BASE_URL else SSConstants.SS_API_BASE_URL

    @Provides
    @Singleton
    fun provideOkhttpClient(
        userDao: UserDao,
        tokenAuthenticator: TokenAuthenticator,
        appConfig: AppConfig
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (appConfig.isDebug) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
        )
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder().also { request ->
                    request.addHeader("Accept", "application/json")
                    userDao.getCurrent()?.let {
                        request.addHeader("x-ss-auth-access-token", it.stsTokenManager.accessToken)
                    }
                }.build()
            )
        }
        .authenticator(tokenAuthenticator)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    internal fun provideMediaApi(
        okHttpClient: OkHttpClient,
        appConfig: AppConfig
    ): SSMediaApi = retrofit(okHttpClient, baseUrl(appConfig))
        .create(SSMediaApi::class.java)

    @Provides
    @Singleton
    internal fun provideQuarterliesApi(
        okHttpClient: OkHttpClient,
        appConfig: AppConfig
    ): SSQuarterliesApi = retrofit(okHttpClient, baseUrl(appConfig))
        .create(SSQuarterliesApi::class.java)

    @Provides
    @Singleton
    internal fun provideLessonsApi(
        okHttpClient: OkHttpClient,
        appConfig: AppConfig
    ): SSLessonsApi = retrofit(okHttpClient, baseUrl(appConfig))
        .create(SSLessonsApi::class.java)
}
