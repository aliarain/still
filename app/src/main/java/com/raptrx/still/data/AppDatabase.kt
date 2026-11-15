package com.raptrx.still.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.raptrx.still.data.entities.AppInfoEntity
import com.raptrx.still.data.entities.AppItemTypeConverter
import com.raptrx.still.data.entities.FocusLimitEntity
import com.raptrx.still.data.entities.FocusScheduleEntity
import com.raptrx.still.data.entities.FocusSchedulePackageEntity
import com.raptrx.still.data.entities.FolderEntity

@Database(
    entities = [
        AppInfoEntity::class,
        FolderEntity::class,
        FocusLimitEntity::class,
        FocusScheduleEntity::class,
        FocusSchedulePackageEntity::class
    ],
    version = 9,
    autoMigrations = [
        AutoMigration(from = 5, to = 6)
    ]
)
@TypeConverters(AppItemTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
    abstract fun folderDao(): FolderDao
    abstract fun focusDao(): FocusDao
}
