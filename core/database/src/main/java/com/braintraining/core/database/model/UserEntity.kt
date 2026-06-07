package com.braintraining.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braintraining.core.model.User
import java.time.LocalDate

@Entity(
    tableName = "users",
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "password")
    val password: String,
    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: LocalDate,
    @ColumnInfo(name = "role")
    val role: String,
)

fun UserEntity.asExternalModel() = User(
    userId = userId,
    username = username,
    password = password,
    dateOfBirth = dateOfBirth,
    role = role,
)

fun User.asEntity() = UserEntity(
    userId = userId,
    username = username,
    password = password,
    dateOfBirth = dateOfBirth,
    role = role,
)
