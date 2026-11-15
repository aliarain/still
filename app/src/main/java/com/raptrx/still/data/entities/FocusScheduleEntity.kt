package com.raptrx.still.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A recurring window during which a set of packages is blocked outright.
 *
 * [startMinutes] and [endMinutes] are minutes from midnight. An end earlier
 * than the start wraps past midnight, so 22:00-07:00 is one continuous window.
 * [daysMask] is a bit per [java.util.Calendar] day of week, where bit 0 is
 * Sunday, matching the calendar constants the scheduler uses.
 */
@Entity(tableName = "focusScheduleEntity")
data class FocusScheduleEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "start_minutes")
    val startMinutes: Int,

    @ColumnInfo(name = "end_minutes")
    val endMinutes: Int,

    @ColumnInfo(name = "days_mask")
    val daysMask: Int,

    @ColumnInfo(name = "is_enabled", defaultValue = "1")
    val isEnabled: Boolean = true
)
