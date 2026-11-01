package com.example.oficiolocal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.oficiolocal.data.local.dao.FavoriteDao
import com.example.oficiolocal.data.local.dao.ReviewDao
import com.example.oficiolocal.data.local.dao.UserDao
import com.example.oficiolocal.data.local.entity.FavoriteEntity
import com.example.oficiolocal.data.local.entity.ReviewEntity
import com.example.oficiolocal.data.local.entity.UserEntity

// Al agregar una entidad nueva: sumarla a la lista, subir 'version' y desinstalar la app del emulador.
@Database(
    entities = [UserEntity::class, FavoriteEntity::class, ReviewEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oficiolocal.db"
                ).build().also { instance = it }
            }
    }
}