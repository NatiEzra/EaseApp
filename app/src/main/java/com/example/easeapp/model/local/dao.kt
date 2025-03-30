package com.example.ease.model.local
import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("DELETE FROM user")
    suspend fun clear()
}