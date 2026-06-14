package com.braintraining.core.model

import java.time.LocalDate

data class UserStats(
    val userId: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastPlayedDate: LocalDate?,
    val firstRating: Int,
    val bestRating: Int,
    val overallRating: Int,
    val globalRank: Int?,
)
