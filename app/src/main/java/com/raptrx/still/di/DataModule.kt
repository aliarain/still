package com.raptrx.still.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.raptrx.still.data.AppDatabase
import com.raptrx.still.data.DatabaseMigrations
import com.raptrx.still.data.PreferenceHelper
import com.raptrx.still.utils.AppUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {
    @Singleton
    @Provides
    fun provideDb(application: Application) =
        Room.databaseBuilder(
            application,
            AppDatabase::class.java, "still-launcher-db"
        )
            .fallbackToDestructiveMigration(true)
            .addMigrations(
                DatabaseMigrations.MIGRATION_1_2(application),
                DatabaseMigrations.MIGRATION_2_3,
                DatabaseMigrations.MIGRATION_3_4,
                DatabaseMigrations.MIGRATION_4_5,
                DatabaseMigrations.MIGRATION_6_7,
                DatabaseMigrations.MIGRATION_7_8,
                DatabaseMigrations.MIGRATION_8_9
            )
            .build()

    @Singleton
    @Provides
    fun providesAppInfoDao(db: AppDatabase) = db.appInfoDao()

    @Singleton
    @Provides
    fun providesFocusDao(db: AppDatabase) = db.focusDao()

    @Singleton
    @Provides
    fun providesFolderDao(db: AppDatabase) = db.folderDao()

    @Singleton
    @Provides
    fun providePreferences(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(drawerAlignmentMigration),
            produceFile = {
                context.preferencesDataStoreFile(
                    "still-launcher-preferences"
                )
            },
        )
    }

    private val drawerAlignmentMigration = object : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean {
            return currentData[PreferenceHelper.KEY_HOME_APPS_ALIGN_HORIZONTAL] != null &&
                    currentData[PreferenceHelper.KEY_DRAWER_APPS_ALIGN_HORIZONTAL] == null
        }

        override suspend fun migrate(currentData: Preferences): Preferences {
            val mutablePrefs = currentData.toMutablePreferences()
            val homeAlignment = currentData[PreferenceHelper.KEY_HOME_APPS_ALIGN_HORIZONTAL]
            if (homeAlignment != null) {
                mutablePrefs[PreferenceHelper.KEY_DRAWER_APPS_ALIGN_HORIZONTAL] = homeAlignment
            }
            return mutablePrefs.toPreferences()
        }

        override suspend fun cleanUp() {
            // No-op
        }
    }

    @Singleton
    @Provides
    fun provideAppUtils(@ApplicationContext context: Context) = AppUtils(context)
}
