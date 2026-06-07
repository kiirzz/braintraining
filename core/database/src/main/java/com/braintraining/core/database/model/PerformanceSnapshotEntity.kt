package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braintraining.core.model.PerformanceSnapshot
import java.time.Instant

@Entity(tableName = "performance_snapshots")
data class PerformanceSnapshotEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "skill_id")
    val skillId: String,
    @ColumnInfo(name = "bpi")
    val bpi: Float,
    @ColumnInfo(name = "taken_at")
    val takenAt: Long,
)

fun PerformanceSnapshotEntity.asExternalModel() = PerformanceSnapshot(
    id = id,
    skillId = skillId,
    bpi = bpi,
    takenAt = Instant.ofEpochMilli(takenAt),
)

fun PerformanceSnapshot.asEntity() = PerformanceSnapshotEntity(
    id = id,
    skillId = skillId,
    bpi = bpi,
    takenAt = takenAt.toEpochMilli(),
)
