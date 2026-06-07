package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braintraining.core.model.PerformanceSnapshot
import com.braintraining.core.model.SkillArea
import java.time.Instant

@Entity(tableName = "performance_snapshots")
data class PerformanceSnapshotEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "skill_area")
    val skillArea: String,
    @ColumnInfo(name = "bpi")
    val bpi: Float,
    @ColumnInfo(name = "taken_at")
    val takenAt: Long,
)

fun PerformanceSnapshotEntity.asExternalModel() = PerformanceSnapshot(
    id = id,
    skillArea = SkillArea.valueOf(skillArea),
    bpi = bpi,
    takenAt = Instant.ofEpochMilli(takenAt),
)

fun PerformanceSnapshot.asEntity() = PerformanceSnapshotEntity(
    id = id,
    skillArea = skillArea.name,
    bpi = bpi,
    takenAt = takenAt.toEpochMilli(),
)
