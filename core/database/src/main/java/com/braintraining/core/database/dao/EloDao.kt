package com.braintraining.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.braintraining.core.database.model.EloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EloDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElo(elo: EloEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElos(elos: List<EloEntity>)

    @Update
    suspend fun updateElo(elo: EloEntity)

    @Delete
    suspend fun deleteElo(elo: EloEntity)

    @Query("SELECT * FROM elo WHERE elo_id = :eloId")
    fun getElo(eloId: String): Flow<EloEntity?>

    @Query("SELECT * FROM elo")
    fun getAllElos(): Flow<List<EloEntity>>

    @Query("DELETE FROM elo WHERE elo_id = :eloId")
    suspend fun deleteEloById(eloId: String)
}
