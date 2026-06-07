package com.braintraining.core.model

import java.time.Instant

data class PerformanceSnapshot(
    val id: String,
    val skillArea: SkillArea,
    val bpi: Float,
    val takenAt: Instant,
)
