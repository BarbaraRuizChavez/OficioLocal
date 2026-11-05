package com.example.oficiolocal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.oficiolocal.data.local.entity.MediaAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaAttachmentDao {
    @Query("SELECT * FROM media_attachments WHERE ownerType = :ownerType AND ownerId = :ownerId ORDER BY createdAt DESC")
    fun observeForOwner(ownerType: String, ownerId: String): Flow<List<MediaAttachmentEntity>>

    @Query("SELECT * FROM media_attachments WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MediaAttachmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attachment: MediaAttachmentEntity)

    @Update
    suspend fun update(attachment: MediaAttachmentEntity)

    @Query("DELETE FROM media_attachments WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM media_attachments WHERE ownerType = :ownerType AND ownerId = :ownerId")
    suspend fun deleteForOwner(ownerType: String, ownerId: String)
}
