package com.braintraining.core.model

import java.time.Instant

data class UserStats(
    val userId: String,
    val eloRating: Int,
    val globalRank: Int?,
    val lastSyncedAt: Instant,
)
