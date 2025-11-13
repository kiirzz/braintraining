package com.braintraining.core.data.repository

import com.braintraining.core.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getGame(id: String): Flow<Game>
    fun getGames(): Flow<List<Game>>
    fun getGameBySkillId(skillId: String): Flow<List<Game>>
    suspend fun refreshGames()
}
