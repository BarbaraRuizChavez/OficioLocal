package com.example.oficiolocal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.oficiolocal.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE providerId = :providerId AND pendingDelete = 0 ORDER BY date DESC, updatedAt DESC")
    fun observeByProvider(providerId: String): Flow<List<ReviewEntity>>

    @Query("SELECT COUNT(*) FROM reviews")
    suspend fun count(): Int

    @Query("SELECT * FROM reviews WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ReviewEntity?

    @Query("SELECT * FROM reviews WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): ReviewEntity?

    @Query("SELECT * FROM reviews WHERE pendingSync = 1 OR pendingDelete = 1")
    suspend fun getPendingSync(): List<ReviewEntity>

    @Insert
    suspend fun insert(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reviews: List<ReviewEntity>)

    @Update
    suspend fun update(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE reviews SET remoteId = :remoteId, pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String, remoteId: String)

    @Query("UPDATE reviews SET pendingDelete = 1, pendingSync = 0 WHERE id = :id")
    suspend fun markDeletePending(id: String)
}
