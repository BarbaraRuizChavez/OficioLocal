package com.example.oficiolocal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.oficiolocal.data.local.dao.FavoriteDao
import com.example.oficiolocal.data.local.dao.MediaAttachmentDao
import com.example.oficiolocal.data.local.dao.NotificationDao
import com.example.oficiolocal.data.local.dao.ProviderDao
import com.example.oficiolocal.data.local.dao.ReviewDao
import com.example.oficiolocal.data.local.dao.ServiceRequestDao
import com.example.oficiolocal.data.local.dao.UserDao
import com.example.oficiolocal.data.local.entity.FavoriteEntity
import com.example.oficiolocal.data.local.entity.MediaAttachmentEntity
import com.example.oficiolocal.data.local.entity.NotificationEntity
import com.example.oficiolocal.data.local.entity.ProviderEntity
import com.example.oficiolocal.data.local.entity.ReviewEntity
import com.example.oficiolocal.data.local.entity.ServiceRequestEntity
import com.example.oficiolocal.data.local.entity.UserEntity

// Al agregar una entidad nueva: sumarla a la lista y subir 'version'.
@Database(
    entities = [
        UserEntity::class, FavoriteEntity::class, ReviewEntity::class,
        ProviderEntity::class, ServiceRequestEntity::class,
        NotificationEntity::class, MediaAttachmentEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun reviewDao(): ReviewDao
    abstract fun providerDao(): ProviderDao
    abstract fun serviceRequestDao(): ServiceRequestDao
    abstract fun notificationDao(): NotificationDao
    abstract fun mediaAttachmentDao(): MediaAttachmentDao

    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `notifications` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `relatedRequestId` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `isRead` INTEGER NOT NULL,
                        `pendingSync` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )""".trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_notifications_createdAt` ON `notifications` (`createdAt`)"
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `media_attachments` (
                        `id` TEXT NOT NULL,
                        `ownerType` TEXT NOT NULL,
                        `ownerId` TEXT NOT NULL,
                        `uri` TEXT NOT NULL,
                        `mimeType` TEXT NOT NULL,
                        `fileName` TEXT,
                        `sizeBytes` INTEGER NOT NULL,
                        `remoteUrl` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )""".trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_media_attachments_ownerType_ownerId` ON `media_attachments` (`ownerType`, `ownerId`)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reviews` ADD COLUMN `remoteId` TEXT")
                db.execSQL("ALTER TABLE `reviews` ADD COLUMN `pendingDelete` INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_reviews_remoteId` ON `reviews` (`remoteId`)"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oficiolocal.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    // Se conserva como respaldo para saltos sin migración definida durante el desarrollo.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { instance = it }
            }
    }
}
