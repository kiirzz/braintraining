package com.braintraining.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.braintraining.core.database.model.RatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RatingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRatings(ratings: List<RatingEntity>)

    @Update
    suspend fun updateRating(rating: RatingEntity)

    @Delete
    suspend fun deleteRating(rating: RatingEntity)

    @Query("SELECT * FROM rating WHERE rating_id = :ratingId")
    fun getRating(ratingId: String): Flow<RatingEntity?>

    @Query("SELECT * FROM rating WHERE user_id = :userId")
    fun getUserRatings(userId: String): Flow<List<RatingEntity>>

    @Query("SELECT * FROM rating WHERE user_id = :userId AND skill_id = :skillId")
    fun getUserSkillRating(userId: String, skillId: String): Flow<RatingEntity?>

    @Query("SELECT * FROM rating WHERE skill_id = :skillId")
    fun getSkillRatings(skillId: String): Flow<List<RatingEntity>>

    @Query("SELECT * FROM rating ORDER BY created_at DESC")
    fun getAllRatings(): Flow<List<RatingEntity>>

    @Query("DELETE FROM rating WHERE rating_id = :ratingId")
    suspend fun deleteRatingById(ratingId: String)

    @Query("DELETE FROM rating WHERE user_id = :userId")
    suspend fun deleteUserRatings(userId: String)
}
