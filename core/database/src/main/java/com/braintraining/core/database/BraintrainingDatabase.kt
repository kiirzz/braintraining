package com.braintraining.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.braintraining.core.database.dao.EloDao
import com.braintraining.core.database.dao.GameDao
import com.braintraining.core.database.dao.RecordDao
import com.braintraining.core.database.dao.SkillDao
import com.braintraining.core.database.dao.UserDao
import com.braintraining.core.database.dao.UserStatsDao
import com.braintraining.core.database.model.EloEntity
import com.braintraining.core.database.model.GameEntity
import com.braintraining.core.database.model.PerformanceSnapshotEntity
import com.braintraining.core.database.model.RecordEntity
import com.braintraining.core.database.model.SkillEntity
import com.braintraining.core.database.model.TrainingSessionEntity
import com.braintraining.core.database.model.TrainingSessionGameEntity
import com.braintraining.core.database.model.UserEntity
import com.braintraining.core.database.model.UserStatsEntity

@Database(
    entities = [
        SkillEntity::class,
        GameEntity::class,
        UserEntity::class,
        RecordEntity::class,
        UserStatsEntity::class,
        EloEntity::class,
        PerformanceSnapshotEntity::class,
        TrainingSessionEntity::class,
        TrainingSessionGameEntity::class,
    ],
    version = 2,
    autoMigrations = [],
    exportSchema = true,
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class BraintrainingDatabase : RoomDatabase() {
    abstract fun skillDao(): SkillDao
    abstract fun gameDao(): GameDao
    abstract fun userDao(): UserDao
    abstract fun recordDao(): RecordDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun eloDao(): EloDao
}
