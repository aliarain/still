package com.raptrx.still.utils

import com.raptrx.still.data.entities.FocusLimitEntity
import com.raptrx.still.data.entities.FocusScheduleEntity
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/** Why a package is not allowed to open right now. */
sealed interface BlockReason {
    /** The package has no limit or schedule covering it. */
    data object None : BlockReason

    /** The daily budget for [packageName] is used up. */
    data class DailyLimit(
        val packageName: String,
        val limitMinutes: Int,
        val usedMinutes: Long
    ) : BlockReason

    /** A focus window such as a bedtime schedule currently covers this package. */
    data class Schedule(
        val packageName: String,
        val scheduleLabel: String
    ) : BlockReason
}

/**
 * Decides whether a package may launch.
 *
 * Both inputs are needed at once: a daily budget is exhausted by elapsed
 * usage, while a schedule is a wall-clock window. A package can be caught by
 * either, so the schedule is reported first as the stronger, unconditional
 * block.
 */
@Singleton
class FocusEnforcer @Inject constructor(
    private val usageTracker: AppUsageTracker
) {
    /**
     * Pure decision step, separated so callers can evaluate a preview without
     * doing any I/O of their own.
     *
     * [schedulePackages] maps a schedule id to the packages it blocks.
     */
    fun evaluate(
        packageName: String,
        limits: List<FocusLimitEntity>,
        schedules: List<FocusScheduleEntity>,
        schedulePackages: Map<String, Set<String>>,
        usedMinutesToday: Long,
        nowMinutes: Int,
        dayOfWeek: Int
    ): BlockReason {
        val schedule = schedules.firstOrNull { schedule ->
            schedule.isEnabled &&
                isActive(schedule, nowMinutes, dayOfWeek) &&
                packageName in schedulePackages[schedule.id].orEmpty()
        }
        if (schedule != null) {
            return BlockReason.Schedule(packageName, schedule.label)
        }

        val limit = limits.firstOrNull { it.packageName == packageName && it.isEnabled }
            ?: return BlockReason.None

        if (usedMinutesToday >= limit.dailyLimitMinutes) {
            return BlockReason.DailyLimit(packageName, limit.dailyLimitMinutes, usedMinutesToday)
        }
        return BlockReason.None
    }

    /** Convenience wrapper that reads live usage for [packageName]. */
    fun reasonForBlocking(
        packageName: String,
        limits: List<FocusLimitEntity>,
        schedules: List<FocusScheduleEntity>,
        schedulePackages: Map<String, Set<String>>
    ): BlockReason {
        val calendar = Calendar.getInstance()
        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val usedMinutes = usageTracker.getTodayMillis(packageName) / 60_000L
        return evaluate(
            packageName = packageName,
            limits = limits,
            schedules = schedules,
            schedulePackages = schedulePackages,
            usedMinutesToday = usedMinutes,
            nowMinutes = nowMinutes,
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        )
    }

    /**
     * Remaining budget in minutes. Zero means the budget is spent.
     */
    fun remainingMinutes(limit: FocusLimitEntity, usedMinutesToday: Long): Long =
        (limit.dailyLimitMinutes - usedMinutesToday).coerceAtLeast(0L)

    /** True when the schedule's window contains [nowMinutes] on [dayOfWeek]. */
    internal fun isActive(
        schedule: FocusScheduleEntity,
        nowMinutes: Int,
        dayOfWeek: Int
    ): Boolean {
        if (schedule.daysMask and dayBit(dayOfWeek) == 0) return false

        return if (schedule.startMinutes <= schedule.endMinutes) {
            nowMinutes >= schedule.startMinutes && nowMinutes < schedule.endMinutes
        } else {
            // The window crosses midnight, so it is live on either side of it.
            nowMinutes >= schedule.startMinutes || nowMinutes < schedule.endMinutes
        }
    }

    /**
     * Calendar.SUNDAY is 1 and SATURDAY is 7, so shifting by day-1 gives one
     * stable bit per weekday.
     */
    internal fun dayBit(dayOfWeek: Int): Int = 1 shl (dayOfWeek - 1)
}
