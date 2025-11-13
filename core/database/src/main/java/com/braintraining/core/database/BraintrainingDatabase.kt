package com.braintraining.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.braintraining.core.database.dao.GameDao
import com.braintraining.core.database.dao.SkillDao
import com.braintraining.core.database.model.GameEntity
import com.braintraining.core.database.model.SkillEntity

@Database(
    entities = [
        SkillEntity::class,
        GameEntity::class,
    ],
    version = 1,
    autoMigrations = [],
    exportSchema = true,
)
abstract class BraintrainingDatabase : RoomDatabase() {
    abstract fun skillDao(): SkillDao
    abstract fun gameDao(): GameDao
}