/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package ss.settings.di

import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ss.settings.SettingsPresenter
import ss.settings.SettingsPresenterFactory
import ss.settings.SettingsUiFactory
import ss.settings.repository.SettingsRepository
import ss.settings.repository.SettingsRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BindingsModule {

  @Binds
  internal abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

  companion object {
    @Provides @IntoSet fun provideSettingsUiFactory(): Ui.Factory = SettingsUiFactory()

    @Provides
    @IntoSet
    fun provideSettingsPresenterFactory(real: SettingsPresenter.Factory): Presenter.Factory =
        SettingsPresenterFactory(real)
  }
}
