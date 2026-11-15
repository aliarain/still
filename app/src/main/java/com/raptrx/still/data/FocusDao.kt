package com.raptrx.still.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.raptrx.still.data.entities.FocusLimitEntity
import com.raptrx.still.data.entities.FocusScheduleEntity
import com.raptrx.still.data.entities.FocusSchedulePackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM focusLimitEntity ORDER BY package_name")
    fun observeLimits(): Flow<List<FocusLimitEntity>>

    @Query("SELECT * FROM focusLimitEntity")
    suspend fun getLimits(): List<FocusLimitEntity>

    @Query("SELECT * FROM focusLimitEntity WHERE package_name = :packageName")
    suspend fun getLimit(packageName: String): FocusLimitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setLimit(limit: FocusLimitEntity)

    @Query("DELETE FROM focusLimitEntity WHERE package_name = :packageName")
    suspend fun clearLimit(packageName: String)

    @Query("SELECT * FROM focusScheduleEntity ORDER BY start_minutes")
    fun observeSchedules(): Flow<List<FocusScheduleEntity>>

    @Query("SELECT * FROM focusScheduleEntity")
    suspend fun getSchedules(): List<FocusScheduleEntity>

    @Query("SELECT * FROM focusSchedulePackageEntity")
    fun observeSchedulePackages(): Flow<List<FocusSchedulePackageEntity>>

    @Query("SELECT * FROM focusSchedulePackageEntity")
    suspend fun getSchedulePackages(): List<FocusSchedulePackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSchedule(schedule: FocusScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSchedulePackages(packages: List<FocusSchedulePackageEntity>)

    @Query("DELETE FROM focusSchedulePackageEntity WHERE schedule_id = :scheduleId")
    suspend fun clearSchedulePackages(scheduleId: String)

    /** Writes a schedule and replaces its package membership atomically. */
    @Transaction
    suspend fun saveSchedule(
        schedule: FocusScheduleEntity,
        packages: List<String>
    ) {
        setSchedule(schedule)
        clearSchedulePackages(schedule.id)
        if (packages.isNotEmpty()) {
            setSchedulePackages(packages.map { FocusSchedulePackageEntity(schedule.id, it) })
        }
    }

    @Query("DELETE FROM focusScheduleEntity WHERE id = :id")
    suspend fun deleteScheduleRow(id: String)

    /** Removes a schedule and its membership together; the FK also cascades. */
    @Transaction
    suspend fun deleteSchedule(id: String) {
        clearSchedulePackages(id)
        deleteScheduleRow(id)
    }
}
