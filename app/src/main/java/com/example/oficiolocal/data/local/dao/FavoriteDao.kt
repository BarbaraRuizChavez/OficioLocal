package com.example.oficiolocal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.oficiolocal.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT providerId FROM favorites WHERE clientId = :clientId")
    fun observeIds(clientId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorites WHERE clientId = :clientId AND providerId = :providerId")
    suspend fun exists(clientId: String, providerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE clientId = :clientId AND providerId = :providerId")
    suspend fun delete(clientId: String, providerId: String)
}