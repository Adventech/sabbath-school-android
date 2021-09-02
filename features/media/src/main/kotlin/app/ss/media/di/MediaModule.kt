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

package app.ss.media.di

import android.app.Application
import android.content.Context
import app.ss.media.BuildConfig
import app.ss.media.api.SSMediaApi
import app.ss.media.repository.SSMediaRepository
import app.ss.media.repository.SSMediaRepositoryImpl
import app.ss.storage.db.dao.AudioDao
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.misc.SSConstants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun okHttpCache(context: Context): Cache {
        val app = context.applicationContext as Application
        return Cache(app.cacheDir, (10 * 1024 * 1024))
    }

    @Provides
    @Singleton
    fun provideOkhttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(okHttpCache(context))
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY else
                    HttpLoggingInterceptor.Level.NONE
            }
        )
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideMediaApi(
        okHttpClient: OkHttpClient
    ): SSMediaApi =
        Retrofit.Builder()
            .baseUrl(SSConstants.SS_API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
            .create(SSMediaApi::class.java)

    @Provides
    @Singleton
    fun provideMediaRepository(
        mediaApi: SSMediaApi,
        audioDao: AudioDao,
        schedulerProvider: SchedulerProvider
    ): SSMediaRepository =
        SSMediaRepositoryImpl(
            mediaApi = mediaApi,
            audioDao = audioDao,
            schedulerProvider = schedulerProvider,
        )
}
