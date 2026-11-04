package com.example.oficiolocal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.oficiolocal.data.local.entity.ServiceRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRequestDao {
    @Query("SELECT * FROM service_requests ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests WHERE id = :id")
    suspend fun getById(id: String): ServiceRequestEntity?

    @Query("SELECT * FROM service_requests WHERE remoteId IS NULL")
    suspend fun getUnsynced(): List<ServiceRequestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ServiceRequestEntity)

    @Query("UPDATE service_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE service_requests SET remoteId = :remoteId WHERE id = :id")
    suspend fun setRemoteId(id: String, remoteId: String)

    @Query("DELETE FROM service_requests WHERE id = :id")
    suspend fun delete(id: String)
}