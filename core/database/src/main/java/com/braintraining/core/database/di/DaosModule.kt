package com.braintraining.core.database.di

import com.braintraining.core.database.BraintrainingDatabase
import com.braintraining.core.database.dao.GameDao
import com.braintraining.core.database.dao.SkillDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
    @Provides
    fun provideSkillsDao(
        database: BraintrainingDatabase,
    ): SkillDao = database.skillDao()

    @Provides
    fun provideGamesDao(
        database: BraintrainingDatabase,
    ): GameDao = database.gameDao()
}