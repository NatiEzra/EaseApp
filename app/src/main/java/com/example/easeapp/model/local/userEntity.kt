package com.example.ease.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val profileImageUrl: String? = null
)

