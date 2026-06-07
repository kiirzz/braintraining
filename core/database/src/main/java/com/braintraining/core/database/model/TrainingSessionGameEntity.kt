package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "training_session_games",
    primaryKeys = ["session_id", "game_id"],
    foreignKeys = [
        ForeignKey(
            entity = TrainingSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
data class TrainingSessionGameEntity(
    @ColumnInfo(name = "session_id", index = true)
    val sessionId: String,
    @ColumnInfo(name = "game_id", index = true)
    val gameId: String,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
)
