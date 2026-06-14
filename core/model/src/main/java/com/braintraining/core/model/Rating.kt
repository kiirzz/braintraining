package com.braintraining.core.model

import java.time.Instant

data class Rating(
    val ratingId: String,
    val userId: String,
    val skillId: String,
    val currentRating: Int,
    val createdAt: Instant,
)
