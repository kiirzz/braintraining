package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.braintraining.core.model.Token

@Entity(
    tableName = "tokens",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class TokenEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "refresh_token")
    val refreshToken: String,
    @ColumnInfo(name = "refresh_expires_at")
    val refreshExpiresAt: Long,
)

fun TokenEntity.asExternalModel() = Token(
    userId = userId,
    refreshToken = refreshToken,
    refreshExpiresAt = refreshExpiresAt,
)

fun Token.asEntity() = TokenEntity(
    userId = userId,
    refreshToken = refreshToken,
    refreshExpiresAt = refreshExpiresAt,
)
