package com.braintraining.core.model

data class Token(
    val userId: String,
    val refreshToken: String,
    val refreshExpiresAt: Long,
)
