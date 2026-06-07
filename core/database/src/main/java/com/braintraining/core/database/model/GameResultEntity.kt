package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.braintraining.core.model.GameResult
import com.braintraining.core.model.SkillArea
import java.time.Instant

@Entity(
    tableName = "game_results",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class GameResultEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "game_id", index = true)
    val gameId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "score")
    val score: Int,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "played_at")
    val playedAt: Long,
    @ColumnInfo(name = "skill_area")
    val skillArea: String,
)

fun GameResultEntity.asExternalModel() = GameResult(
    id = id,
    gameId = gameId,
    userId = userId,
    score = score,
    durationMs = durationMs,
    playedAt = Instant.ofEpochMilli(playedAt),
    skillArea = SkillArea.valueOf(skillArea),
)

fun GameResult.asEntity() = GameResultEntity(
    id = id,
    gameId = gameId,
    userId = userId,
    score = score,
    durationMs = durationMs,
    playedAt = playedAt.toEpochMilli(),
    skillArea = skillArea.name,
)
