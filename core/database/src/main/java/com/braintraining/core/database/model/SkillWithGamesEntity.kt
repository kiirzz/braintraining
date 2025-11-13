package com.braintraining.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.braintraining.core.model.SkillWithGame

data class SkillWithGamesEntity(
    @Embedded val skill: SkillEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "skill_id"
    )
    val games: List<GameEntity>
)

fun SkillWithGamesEntity.asExternalModel() = SkillWithGame(
    skill = skill.asExternalModel(),
    games = games.map { it.asExternalModel() }
)
