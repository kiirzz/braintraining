package com.braintraining.core.data.repository

import com.braintraining.core.model.Skill
import com.braintraining.core.model.SkillWithGame
import kotlinx.coroutines.flow.Flow

interface SkillRepository {
    fun getSkill(id: String): Flow<Skill>
    fun getSkills(): Flow<List<Skill>>
    fun getSkillsWithGames(): Flow<List<SkillWithGame>>
}