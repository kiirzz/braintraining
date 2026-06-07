package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.braintraining.core.model.UserStats

@Entity(
    tableName = "user_stats",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class UserStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "current_streak")
    val currentStreak: Int,
    @ColumnInfo(name = "best_streak")
    val bestStreak: Int,
    @ColumnInfo(name = "first_elo")
    val firstElo: Int,
    @ColumnInfo(name = "best_elo")
    val bestElo: Int,
    @ColumnInfo(name = "overall_elo")
    val overallElo: Int,
)

fun UserStatsEntity.asExternalModel() = UserStats(
    userId = userId,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    firstElo = firstElo,
    bestElo = bestElo,
    overallElo = overallElo,
)

fun UserStats.asEntity() = UserStatsEntity(
    userId = userId,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    firstElo = firstElo,
    bestElo = bestElo,
    overallElo = overallElo,
)
