package com.braintraining.core.model

import java.time.Instant

data class GameResult(
    val id: String,
    val gameId: String,
    val userId: String,
    val score: Int,
    val durationMs: Long,
    val playedAt: Instant,
    val skillArea: SkillArea,
)
