package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braintraining.core.model.Skill
import com.braintraining.core.model.SkillWithGame

@Entity(
    tableName = "skills",
)

data class SkillEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
)

fun SkillEntity.asExternalModel() = Skill(
    id = id,
    name = name,
)