package com.cryart.sabbathschool.test.di

import app.ss.auth.AuthRepository
import app.ss.auth.di.AuthBindings
import app.ss.lessons.data.di.RepositoryModule
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import com.cryart.sabbathschool.test.di.repository.FakeAuthRepository
import com.cryart.sabbathschool.test.di.repository.FakeLessonsRepository
import com.cryart.sabbathschool.test.di.repository.FakeMediaRepository
import com.cryart.sabbathschool.test.di.repository.FakeQuarterliesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        RepositoryModule::class,
        AuthBindings::class
    ]
)
abstract class FakeRepositoryModule {
    @Binds
    abstract fun bindQuarterliesRepository(impl: FakeQuarterliesRepository): QuarterliesRepository

    @Binds
    abstract fun bindLessonsRepository(impl: FakeLessonsRepository): LessonsRepository

    @Binds
    abstract fun bindMediaRepository(impl: FakeMediaRepository): MediaRepository

    @Binds
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
}
