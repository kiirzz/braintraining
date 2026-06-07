package com.braintraining.core.model

import java.time.LocalDate

data class UserStats(
    val userId: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastPlayedDate: LocalDate?,
    val firstElo: Int,
    val bestElo: Int,
    val overallElo: Int,
    val globalRank: Int?,
)
