package com.example.ease.model.local

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM user LIMIT 1")
    fun getCurrentUserBlocking(): UserEntity?

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE user SET phoneNumber = :phone, dateOfBirth = :dob, gender = :gender WHERE email = :email")
    suspend fun updateProfileData(email: String, phone: String, dob: String, gender: String)

    @Query("DELETE FROM user")
    suspend fun clear()
}