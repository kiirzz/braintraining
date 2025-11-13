package com.braintraining.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.braintraining.core.database.model.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query(
        value = """
            SELECT * FROM games
            WHERE id = :gameId
        """
    )
    fun getGameEntity(gameId: String): Flow<GameEntity>

    @Query("SELECT * FROM games")
    fun getGameEntities(): Flow<List<GameEntity>>

    @Query("""
            SELECT * FROM games 
            WHERE skill_id = :skillId
        """)
    fun getGameBySkillId(skillId: String): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreGames(gameEntities: List<GameEntity>): List<Long>

    @Upsert
    suspend fun upsertGames(gameEntities: List<GameEntity>)

    @Query(
        value = """
            DELETE FROM games
            WHERE id in (:ids)
        """
    )
    suspend fun deleteGames(ids: List<String>)
}