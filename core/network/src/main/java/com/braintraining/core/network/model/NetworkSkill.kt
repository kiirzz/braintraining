package com.braintraining.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkSkill(
    val id: String,
    val name: String,
)