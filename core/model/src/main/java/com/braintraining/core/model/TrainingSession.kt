package com.braintraining.core.model

import java.time.LocalDate

data class TrainingSession(
    val id: String,
    val date: LocalDate,
    val games: List<Game>,
    val completedGameIds: Set<String>,
) {
    val isCompleted: Boolean get() = completedGameIds.size >= games.size
}
