package app.ss.widgets.di

import android.content.Context
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.widgets.AppWidgetHelper
import app.ss.widgets.AppWidgetHelperImpl
import app.ss.widgets.WidgetDataProvider
import app.ss.widgets.WidgetDataProviderImpl
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object WidgetModule {

    @Provides
    @Singleton
    internal fun provideWidgetDataProvider(
        repository: LessonsRepository,
        schedulerProvider: SchedulerProvider,
    ): WidgetDataProvider = WidgetDataProviderImpl(
        repository = repository,
        schedulerProvider = schedulerProvider,
    )

    @Provides
    @Singleton
    fun providerWidgetHelper(
        @ApplicationContext context: Context
    ): AppWidgetHelper = AppWidgetHelperImpl(context)
}
