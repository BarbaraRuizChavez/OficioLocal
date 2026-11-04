package com.example.oficiolocal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.oficiolocal.data.local.entity.ProviderEntity

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY id")
    suspend fun getAll(): List<ProviderEntity>

    @Query("SELECT COUNT(*) FROM providers")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProviderEntity>)

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun delete(id: String)
}