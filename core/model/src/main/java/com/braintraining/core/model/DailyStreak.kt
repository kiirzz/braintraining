package com.braintraining.core.model

import java.time.LocalDate

data class DailyStreak(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastPlayedDate: LocalDate?,
)
