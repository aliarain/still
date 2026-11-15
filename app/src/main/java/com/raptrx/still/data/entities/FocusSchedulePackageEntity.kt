package com.raptrx.still.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Which packages a [FocusScheduleEntity] blocks while its window is open.
 *
 * A join table keeps schedule membership independent of daily budgets, so a
 * package can be blocked by a bedtime window without also having a limit.
 */
@Entity(
    tableName = "focusSchedulePackageEntity",
    primaryKeys = ["schedule_id", "package_name"],
    foreignKeys = [ForeignKey(
        entity = FocusScheduleEntity::class,
        parentColumns = ["id"],
        childColumns = ["schedule_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("schedule_id")]
)
data class FocusSchedulePackageEntity(
    @ColumnInfo(name = "schedule_id")
    val scheduleId: String,

    @ColumnInfo(name = "package_name")
    val packageName: String
)
