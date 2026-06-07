package com.braintraining.core.model

data class UserPreferences(
    val dailyGameGoal: Int = 3,
    val reminderEnabled: Boolean = false,
    val reminderTimeMinutes: Int = 480,
    val useDarkTheme: Boolean = false,
)
