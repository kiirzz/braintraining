package com.braintraining.core.model

import java.time.LocalDate

data class User(
    val userId: String,
    val username: String,
    val password: String,
    val dateOfBirth: LocalDate,
    val role: String,
)
