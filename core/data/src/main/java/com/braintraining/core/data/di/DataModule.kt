package com.braintraining.core.data.di

import com.braintraining.core.data.repository.DefaultGameRepository
import com.braintraining.core.data.repository.DefaultSkillRepository
import com.braintraining.core.data.repository.GameRepository
import com.braintraining.core.data.repository.SkillRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    internal abstract fun bindGameRepository(
        gameRepository: DefaultGameRepository
    ): GameRepository

    @Binds
    @Singleton
    internal abstract fun bindSkillRepository(
        skillRepository: DefaultSkillRepository
    ): SkillRepository
}
