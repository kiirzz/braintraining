package com.braintraining.core.database.di

import com.braintraining.core.database.BraintrainingDatabase
import com.braintraining.core.database.dao.EloDao
import com.braintraining.core.database.dao.GameDao
import com.braintraining.core.database.dao.RecordDao
import com.braintraining.core.database.dao.SkillDao
import com.braintraining.core.database.dao.UserDao
import com.braintraining.core.database.dao.UserStatsDao
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

    @Provides
    fun provideUserDao(
        database: BraintrainingDatabase,
    ): UserDao = database.userDao()

    @Provides
    fun provideRecordDao(
        database: BraintrainingDatabase,
    ): RecordDao = database.recordDao()

    @Provides
    fun provideUserStatsDao(
        database: BraintrainingDatabase,
    ): UserStatsDao = database.userStatsDao()

    @Provides
    fun provideEloDao(
        database: BraintrainingDatabase,
    ): EloDao = database.eloDao()
}