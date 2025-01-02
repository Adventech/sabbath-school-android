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

package ss.document.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import ss.document.producer.FontFamilyProviderImpl
import ss.document.producer.OverlayStateProducer
import ss.document.producer.OverlayStateProducerImpl
import ss.document.producer.TopAppbarActionsProducer
import ss.document.producer.TopAppbarActionsProducerImpl

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BindingsModule {
    @Binds
    internal abstract fun bindTopAppbarActionsProducer(impl: TopAppbarActionsProducerImpl): TopAppbarActionsProducer

    @Binds
    internal abstract fun bindFontFamilyProvider(impl: FontFamilyProviderImpl): FontFamilyProvider

    @Binds
    internal abstract fun bindOverlayStateProducer(impl: OverlayStateProducerImpl): OverlayStateProducer
}
