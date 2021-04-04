package com.cryart.sabbathschool.test.di

import app.ss.lessons.data.di.RepositoryModule
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import com.cryart.sabbathschool.test.di.repository.FakeLessonsRepository
import com.cryart.sabbathschool.test.di.repository.FakeQuarterliesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class FakeRepositoryModule {
    @Binds
    abstract fun bindQuarterliesRepository(impl: FakeQuarterliesRepository): QuarterliesRepository

    @Binds
    abstract fun bindLessonsRepository(impl: FakeLessonsRepository): LessonsRepository
}
