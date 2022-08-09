package app.ss.widgets.di

import app.ss.widgets.AppWidgetHelper
import app.ss.widgets.AppWidgetHelperImpl
import app.ss.widgets.WidgetDataProvider
import app.ss.widgets.WidgetDataProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    internal abstract fun bindWidgetDataProvider(impl: WidgetDataProviderImpl): WidgetDataProvider

    @Binds
    internal abstract fun bindWidgetHelper(impl: AppWidgetHelperImpl): AppWidgetHelper
}
