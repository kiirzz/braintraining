package com.braintraining.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.braintraining.core.database.model.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(userStats: UserStatsEntity)

    @Delete
    suspend fun deleteUserStats(userStats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE user_id = :userId")
    fun getUserStats(userId: String): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats")
    fun getAllUserStats(): Flow<List<UserStatsEntity>>

    @Query("DELETE FROM user_stats WHERE user_id = :userId")
    suspend fun deleteUserStatsByUserId(userId: String)
}
