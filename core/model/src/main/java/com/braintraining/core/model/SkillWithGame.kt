package com.braintraining.core.model

import androidx.room.Embedded
import androidx.room.Relation

data class SkillWithGame(
    @Embedded val skill: Skill,
    @Relation(parentColumn = "id", entityColumn = "skill_id")
    val games: List<Game>
)