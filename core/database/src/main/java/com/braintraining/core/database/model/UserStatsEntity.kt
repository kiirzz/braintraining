package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.braintraining.core.model.UserStats
import java.time.LocalDate

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
    @ColumnInfo(name = "longest_streak")
    val longestStreak: Int,
    @ColumnInfo(name = "last_played_date")
    val lastPlayedDate: LocalDate?,
    @ColumnInfo(name = "first_rating")
    val firstRating: Int,
    @ColumnInfo(name = "best_rating")
    val bestRating: Int,
    @ColumnInfo(name = "overall_rating")
    val overallRating: Int,
    @ColumnInfo(name = "global_rank")
    val globalRank: Int?,
)

fun UserStatsEntity.asExternalModel() = UserStats(
    userId = userId,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastPlayedDate = lastPlayedDate,
    firstRating = firstRating,
    bestRating = bestRating,
    overallRating = overallRating,
    globalRank = globalRank,
)

fun UserStats.asEntity() = UserStatsEntity(
    userId = userId,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastPlayedDate = lastPlayedDate,
    firstRating = firstRating,
    bestRating = bestRating,
    overallRating = overallRating,
    globalRank = globalRank,
)
