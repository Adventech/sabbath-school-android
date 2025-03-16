/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.lessons.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ss.lessons.api.ContentSyncProvider
import ss.lessons.api.helper.SyncHelper
import ss.lessons.api.repository.LessonsRepository
import ss.lessons.api.repository.QuarterliesRepository
import ss.lessons.impl.ContentSyncProviderImpl
import ss.lessons.impl.helper.SyncHelperImpl
import ss.lessons.impl.repository.LessonsRepositoryImpl
import ss.lessons.impl.repository.QuarterliesRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    internal abstract fun bindLessonsRepository(impl: LessonsRepositoryImpl): LessonsRepository

    @Binds
    internal abstract fun bindQuarterliesRepository(impl: QuarterliesRepositoryImpl): QuarterliesRepository

    @Binds
    internal abstract fun bindContentSyncProvider(impl: ContentSyncProviderImpl): ContentSyncProvider

    @Binds
    internal abstract fun bindSyncHelper(impl: SyncHelperImpl): SyncHelper
}
