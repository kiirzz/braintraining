package com.braintraining.core.model

import java.time.Instant

data class Elo(
    val eloId: String,
    val userId: String,
    val skillId: String,
    val currentElo: Int,
    val createdAt: Instant,
)
