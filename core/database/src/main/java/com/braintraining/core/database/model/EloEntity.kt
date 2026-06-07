package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braintraining.core.model.Elo
import java.time.Instant

@Entity(
    tableName = "elo",
)
data class EloEntity(
    @PrimaryKey
    @ColumnInfo(name = "elo_id")
    val eloId: String,
    @ColumnInfo(name = "current_elo")
    val currentElo: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
)

fun EloEntity.asExternalModel() = Elo(
    eloId = eloId,
    currentElo = currentElo,
    createdAt = createdAt,
)

fun Elo.asEntity() = EloEntity(
    eloId = eloId,
    currentElo = currentElo,
    createdAt = createdAt,
)
