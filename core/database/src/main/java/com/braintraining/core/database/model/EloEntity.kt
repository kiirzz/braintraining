package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.braintraining.core.model.Elo
import java.time.Instant

@Entity(
    tableName = "elo",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
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
data class EloEntity(
    @PrimaryKey
    @ColumnInfo(name = "elo_id")
    val eloId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "skill_id")
    val skillId: String,
    @ColumnInfo(name = "current_elo")
    val currentElo: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

fun EloEntity.asExternalModel() = Elo(
    eloId = eloId,
    userId = userId,
    skillId = skillId,
    currentElo = currentElo,
    createdAt = Instant.ofEpochMilli(createdAt),
)

fun Elo.asEntity() = EloEntity(
    eloId = eloId,
    userId = userId,
    skillId = skillId,
    currentElo = currentElo,
    createdAt = createdAt.toEpochMilli(),
)
