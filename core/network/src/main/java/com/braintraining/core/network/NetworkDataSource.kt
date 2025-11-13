package com.braintraining.core.network

import com.braintraining.core.model.Game
import com.braintraining.core.model.Skill
import com.braintraining.core.network.api.GameApi
import com.braintraining.core.network.api.SkillApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkDataSource @Inject constructor(
    private val gameApi: GameApi,
    private val skillApi: SkillApi
) {
    suspend fun fetchGame() = gameApi.getGames()
    suspend fun fetchGame(id: String) = gameApi.getGameById(id)
    suspend fun updateGame(id: String, game: Game) = gameApi.updateGame(id, game)
    suspend fun deleteGame(id: String) = gameApi.deleteGame(id)

    suspend fun fetchSkill() = skillApi.getSkills()
    suspend fun fetchSkill(id: String) = skillApi.getSkillById(id)
    suspend fun updateSkill(id: String, skill: Skill) = skillApi.updateSkill(id, skill)
    suspend fun deleteSkill(id: String) = skillApi.deleteSkill(id)
}