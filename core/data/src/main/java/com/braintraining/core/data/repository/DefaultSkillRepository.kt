package com.braintraining.core.data.repository

import com.braintraining.core.database.dao.SkillDao
import com.braintraining.core.database.model.SkillEntity
import com.braintraining.core.database.model.SkillWithGamesEntity
import com.braintraining.core.database.model.asExternalModel
import com.braintraining.core.model.Skill
import com.braintraining.core.model.SkillWithGame
import com.braintraining.core.network.NetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultSkillRepository @Inject constructor(
    private val skillDao: SkillDao,
    private val network: NetworkDataSource
) : SkillRepository {
    override fun getSkill(id: String): Flow<Skill> {
        return skillDao.getSkillEntity(id)
            .map { it.asExternalModel() }
    }

    override fun getSkills(): Flow<List<Skill>> {
        return skillDao.getSkillEntities()
            .map { it.map(SkillEntity::asExternalModel) }
    }

    override fun getSkillsWithGames(): Flow<List<SkillWithGame>> {
        return skillDao.getSkillsWithGames()
            .map { entities: List<SkillWithGamesEntity> ->
                entities.map { it.asExternalModel() }
            }
    }
}