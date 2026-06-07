package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.braintraining.core.model.Record
import java.time.Instant

@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skill_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class RecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "score")
    val score: Int,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "played_at")
    val playedAt: Long,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "game_id")
    val gameId: String,
    @ColumnInfo(name = "skill_id")
    val skillId: String,
)

fun RecordEntity.asExternalModel() = Record(
    recordId = recordId,
    score = score,
    durationMs = durationMs,
    playedAt = Instant.ofEpochMilli(playedAt),
    userId = userId,
    gameId = gameId,
    skillId = skillId,
)

fun Record.asEntity() = RecordEntity(
    recordId = recordId,
    score = score,
    durationMs = durationMs,
    playedAt = playedAt.toEpochMilli(),
    userId = userId,
    gameId = gameId,
    skillId = skillId,
)
