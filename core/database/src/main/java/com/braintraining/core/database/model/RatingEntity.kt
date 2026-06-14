package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.braintraining.core.model.Rating
import java.time.Instant

@Entity(
    tableName = "rating",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skill_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class RatingEntity(
    @PrimaryKey
    @ColumnInfo(name = "rating_id")
    val ratingId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "skill_id")
    val skillId: String,
    @ColumnInfo(name = "current_rating")
    val currentRating: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

fun RatingEntity.asExternalModel() = Rating(
    ratingId = ratingId,
    userId = userId,
    skillId = skillId,
    currentRating = currentRating,
    createdAt = Instant.ofEpochMilli(createdAt),
)

fun Rating.asEntity() = RatingEntity(
    ratingId = ratingId,
    userId = userId,
    skillId = skillId,
    currentRating = currentRating,
    createdAt = createdAt.toEpochMilli(),
)
