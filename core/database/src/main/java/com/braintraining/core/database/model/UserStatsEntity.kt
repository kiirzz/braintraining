package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braintraining.core.model.UserStats
import java.time.Instant

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "elo_rating")
    val eloRating: Int,
    // null until first global ranking sync
    @ColumnInfo(name = "global_rank")
    val globalRank: Int?,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long,
)

fun UserStatsEntity.asExternalModel() = UserStats(
    userId = userId,
    eloRating = eloRating,
    globalRank = globalRank,
    lastSyncedAt = Instant.ofEpochMilli(lastSyncedAt),
)

fun UserStats.asEntity() = UserStatsEntity(
    userId = userId,
    eloRating = eloRating,
    globalRank = globalRank,
    lastSyncedAt = lastSyncedAt.toEpochMilli(),
)
