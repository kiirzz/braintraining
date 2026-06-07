package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.braintraining.core.model.GameRecord
import java.time.LocalDate

@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class RecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "play_time")
    val playTime: Int,
    @ColumnInfo(name = "score")
    val score: Int,
    @ColumnInfo(name = "date_played")
    val datePlayed: LocalDate,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "game_id")
    val gameId: String,
)

fun RecordEntity.asExternalModel() = GameRecord(
    recordId = recordId,
    playTime = playTime,
    score = score,
    datePlayed = datePlayed,
    userId = userId,
    gameId = gameId,
)

fun GameRecord.asEntity() = RecordEntity(
    recordId = recordId,
    playTime = playTime,
    score = score,
    datePlayed = datePlayed,
    userId = userId,
    gameId = gameId,
)
