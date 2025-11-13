package com.braintraining.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkGame(
    val id: String,
    val name: String,
    val description: String,
    val skillId: String,
)