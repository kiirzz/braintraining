package com.braintraining.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.braintraining.core.database.model.SkillEntity
import com.braintraining.core.database.model.SkillWithGamesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query(
        value = """
            SELECT * FROM skills
            WHERE id = :skillId
        """
    )
    fun getSkillEntity(skillId: String): Flow<SkillEntity>

    @Query(value = "SELECT * FROM skills")
    fun getSkillEntities(): Flow<List<SkillEntity>>

    @Transaction
    @Query("SELECT * FROM skills")
    fun getSkillsWithGames(): Flow<List<SkillWithGamesEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreSkills(skillEntities: List<SkillEntity>): List<Long>

    @Upsert
    suspend fun upsertSkills(skillEntities: List<SkillEntity>)

    @Query(
        value = """
            DELETE FROM skills
            WHERE id in (:ids)
        """
    )
    suspend fun deleteSkills(ids: List<String>)
}