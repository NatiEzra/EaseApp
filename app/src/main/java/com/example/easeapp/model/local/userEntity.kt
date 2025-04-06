package com.example.ease.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val _id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val accessToken: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null
)