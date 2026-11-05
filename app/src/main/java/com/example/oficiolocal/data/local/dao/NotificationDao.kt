package com.example.oficiolocal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.oficiolocal.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
