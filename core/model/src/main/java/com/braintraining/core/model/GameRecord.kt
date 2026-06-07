package com.braintraining.core.model

import java.time.LocalDate

data class GameRecord(
    val recordId: String,
    val playTime: Int,
    val score: Int,
    val datePlayed: LocalDate,
    val userId: String,
    val gameId: String,
)
