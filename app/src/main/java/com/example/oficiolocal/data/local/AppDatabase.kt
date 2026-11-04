package com.example.oficiolocal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.oficiolocal.data.local.dao.FavoriteDao
import com.example.oficiolocal.data.local.dao.ProviderDao
import com.example.oficiolocal.data.local.dao.ReviewDao
import com.example.oficiolocal.data.local.dao.ServiceRequestDao
import com.example.oficiolocal.data.local.dao.UserDao
import com.example.oficiolocal.data.local.entity.FavoriteEntity
import com.example.oficiolocal.data.local.entity.ProviderEntity
import com.example.oficiolocal.data.local.entity.ReviewEntity
import com.example.oficiolocal.data.local.entity.ServiceRequestEntity
import com.example.oficiolocal.data.local.entity.UserEntity

// Al agregar una entidad nueva: sumarla a la lista y subir 'version'.
@Database(
    entities = [
        UserEntity::class, FavoriteEntity::class, ReviewEntity::class,
        ProviderEntity::class, ServiceRequestEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun reviewDao(): ReviewDao
    abstract fun providerDao(): ProviderDao
    abstract fun serviceRequestDao(): ServiceRequestDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oficiolocal.db"
                )
                    // Al subir la versión se borra la base local y se vuelve a llenar (solo en desarrollo).
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { instance = it }
            }
    }
}