package com.braintraining.core.model

data class UserStats(
    val userId: String,
    val currentStreak: Int,
    val bestStreak: Int,
    val firstElo: Int,
    val bestElo: Int,
    val overallElo: Int,
)
