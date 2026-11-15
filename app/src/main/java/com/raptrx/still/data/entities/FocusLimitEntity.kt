package com.raptrx.still.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A daily usage budget for one package.
 *
 * [dailyLimitMinutes] is the total foreground time allowed per calendar day.
 * A value of 0 means the app is blocked outright, which is how Focus Mode
 * limits the packages in its active session.
 */
@Entity(tableName = "focusLimitEntity")
data class FocusLimitEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "daily_limit_minutes")
    val dailyLimitMinutes: Int,

    @ColumnInfo(name = "is_enabled", defaultValue = "1")
    val isEnabled: Boolean = true
)
