package com.braintraining.core.data.repository

import com.braintraining.core.database.dao.GameDao
import com.braintraining.core.database.model.GameEntity
import com.braintraining.core.database.model.asExternalModel
import com.braintraining.core.database.model.asEntity
import com.braintraining.core.model.Game
import com.braintraining.core.network.NetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultGameRepository @Inject constructor(
    private val gameDao: GameDao,
    private val network: NetworkDataSource
) : GameRepository {
    override fun getGame(id: String): Flow<Game> {
        return gameDao.getGameEntity(id)
            .map { it.asExternalModel() }
    }

    override fun getGames(): Flow<List<Game>> {
        return gameDao.getGameEntities()
            .map { it.map(GameEntity::asExternalModel) }
    }

    override fun getGameBySkillId(skillId: String): Flow<List<Game>> {
        return gameDao.getGameBySkillId(skillId)
            .map { it.map(GameEntity::asExternalModel) }
    }

    override suspend fun refreshGames() {
        val remoteGames = network.fetchGame()
        gameDao.upsertGames(remoteGames.map { it.asEntity() })

    }
}
