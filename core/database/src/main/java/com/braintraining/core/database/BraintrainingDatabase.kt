package com.braintraining.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.braintraining.core.database.dao.GameDao
import com.braintraining.core.database.dao.SkillDao
import com.braintraining.core.database.model.DailyStreakEntity
import com.braintraining.core.database.model.GameEntity
import com.braintraining.core.database.model.GameResultEntity
import com.braintraining.core.database.model.PerformanceSnapshotEntity
import com.braintraining.core.database.model.SkillEntity
import com.braintraining.core.database.model.TrainingSessionEntity
import com.braintraining.core.database.model.TrainingSessionGameEntity
import com.braintraining.core.database.model.UserStatsEntity

@Database(
    entities = [
        SkillEntity::class,
        GameEntity::class,
        GameResultEntity::class,
        PerformanceSnapshotEntity::class,
        TrainingSessionEntity::class,
        TrainingSessionGameEntity::class,
        DailyStreakEntity::class,
        UserStatsEntity::class,
    ],
    version = 2,
    autoMigrations = [],
    exportSchema = true,
)
abstract class BraintrainingDatabase : RoomDatabase() {
    abstract fun skillDao(): SkillDao
    abstract fun gameDao(): GameDao
}
