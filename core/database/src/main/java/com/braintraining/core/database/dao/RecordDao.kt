package com.braintraining.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.braintraining.core.database.model.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<RecordEntity>)

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("SELECT * FROM records WHERE record_id = :recordId")
    fun getRecord(recordId: String): Flow<RecordEntity?>

    @Query("SELECT * FROM records WHERE user_id = :userId ORDER BY played_at DESC")
    fun getUserRecords(userId: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE game_id = :gameId ORDER BY played_at DESC")
    fun getGameRecords(gameId: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE user_id = :userId AND skill_area = :skillArea ORDER BY played_at DESC")
    fun getUserRecordsBySkillArea(userId: String, skillArea: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records ORDER BY played_at DESC")
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE user_id = :userId ORDER BY played_at DESC LIMIT :limit")
    fun getUserRecentRecords(userId: String, limit: Int): Flow<List<RecordEntity>>

    @Query("DELETE FROM records WHERE record_id = :recordId")
    suspend fun deleteRecordById(recordId: String)

    @Query("DELETE FROM records WHERE user_id = :userId")
    suspend fun deleteUserRecords(userId: String)
}

