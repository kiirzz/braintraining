package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braintraining.core.model.DailyStreak
import java.time.LocalDate

@Entity(tableName = "daily_streaks")
data class DailyStreakEntity(
    // always 1 — this table is a singleton
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,
    @ColumnInfo(name = "current_streak")
    val currentStreak: Int,
    @ColumnInfo(name = "longest_streak")
    val longestStreak: Int,
    // stored as epoch day (LocalDate.toEpochDay()), null if never played
    @ColumnInfo(name = "last_played_date")
    val lastPlayedDate: Long?,
)

fun DailyStreakEntity.asExternalModel() = DailyStreak(
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastPlayedDate = lastPlayedDate?.let { LocalDate.ofEpochDay(it) },
)

fun DailyStreak.asEntity() = DailyStreakEntity(
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastPlayedDate = lastPlayedDate?.toEpochDay(),
)
