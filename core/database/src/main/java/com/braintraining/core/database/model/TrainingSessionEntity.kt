package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    // stored as epoch day (LocalDate.toEpochDay())
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
)
