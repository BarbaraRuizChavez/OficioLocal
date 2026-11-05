package com.example.oficiolocal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.oficiolocal.data.local.entity.ServiceRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRequestDao {
    @Query("SELECT * FROM service_requests ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests WHERE id = :id")
    suspend fun getById(id: String): ServiceRequestEntity?

    @Query("SELECT * FROM service_requests WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): ServiceRequestEntity?

    @Query("SELECT * FROM service_requests WHERE remoteId IS NULL")
    suspend fun getUnsynced(): List<ServiceRequestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ServiceRequestEntity)

    @Update
    suspend fun update(item: ServiceRequestEntity)

    @Transaction
    suspend fun mergeRemote(items: List<ServiceRequestEntity>) {
        items.forEach { incoming ->
            val remoteId = incoming.remoteId ?: return@forEach
            val existing = getByRemoteId(remoteId)
            val merged = incoming.copy(
                id = existing?.id ?: incoming.id,
                createdAt = existing?.createdAt ?: incoming.createdAt
            )
            if (existing == null) insert(merged) else update(merged)
        }
    }

    @Query("UPDATE service_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE service_requests SET remoteId = :remoteId WHERE id = :id")
    suspend fun setRemoteId(id: String, remoteId: String)

    @Query("DELETE FROM service_requests WHERE id = :id")
    suspend fun delete(id: String)
}
