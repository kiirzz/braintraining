package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import com.braintraining.core.model.Game

@Entity(
    tableName = "games",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skill_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)

data class GameEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "skill_id")
    val skillId: String,
)

fun GameEntity.asExternalModel() = Game(
    id = id,
    name = name,
    description = description,
    skillId = skillId
)

fun Game.asEntity(): GameEntity{
    return GameEntity(
        id = id,
        name = name,
        description = description,
        skillId = skillId
    )
}