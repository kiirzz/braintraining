package com.braintraining.core.model

import java.time.Instant

data class PerformanceSnapshot(
    val id: String,
    val skillId: String,
    val bpi: Float,
    val takenAt: Instant,
)
