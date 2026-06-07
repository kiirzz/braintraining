package com.braintraining.core.model

import java.time.Instant

data class Record(
    val recordId: String,
    val score: Int,
    val durationMs: Long,
    val playedAt: Instant,
    val userId: String,
    val gameId: String,
    val skillArea: SkillArea,
)
