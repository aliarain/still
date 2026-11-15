package com.raptrx.still.data

import com.raptrx.still.data.entities.FocusLimitEntity
import com.raptrx.still.data.entities.FocusScheduleEntity
import com.raptrx.still.utils.AppUsageTracker
import com.raptrx.still.utils.BlockReason
import com.raptrx.still.utils.FocusEnforcer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** A package's budget combined with the time already spent against it. */
data class LimitStatus(
    val packageName: String,
    val limitMinutes: Int,
    val usedMinutes: Long,
    val isEnabled: Boolean
) {
    val remainingMinutes: Long get() = (limitMinutes - usedMinutes).coerceAtLeast(0L)
    val isExhausted: Boolean get() = usedMinutes >= limitMinutes
    val usedFraction: Float
        get() = if (limitMinutes <= 0) 1f
        else (usedMinutes.toFloat() / limitMinutes).coerceIn(0f, 1f)
}

@Singleton
class FocusRepository @Inject constructor(
    private val focusDao: FocusDao,
    private val enforcer: FocusEnforcer,
    private val usageTracker: AppUsageTracker
) {
    val limits: Flow<List<FocusLimitEntity>> = focusDao.observeLimits()

    val schedules: Flow<List<FocusScheduleEntity>> = focusDao.observeSchedules()

    private val schedulePackages: Flow<Map<String, Set<String>>> =
        focusDao.observeSchedulePackages().map { rows ->
            rows.groupBy { it.scheduleId }.mapValues { (_, v) -> v.map { it.packageName }.toSet() }
        }

    /** Live status for every limited package, including today's usage. */
    fun observeLimitStatuses(): Flow<List<LimitStatus>> =
        combine(focusDao.observeLimits(), focusDao.observeSchedulePackages()) { limits, _ ->
            val usage = usageTracker.getForegroundMillisSince(usageTracker.startOfToday())
            limits.map { limit ->
                LimitStatus(
                    packageName = limit.packageName,
                    limitMinutes = limit.dailyLimitMinutes,
                    usedMinutes = (usage[limit.packageName] ?: 0L) / 60_000L,
                    isEnabled = limit.isEnabled
                )
            }.sortedBy { it.remainingMinutes }
        }

    suspend fun setLimit(packageName: String, minutes: Int, enabled: Boolean = true) =
        focusDao.setLimit(
            FocusLimitEntity(
                packageName = packageName,
                dailyLimitMinutes = minutes,
                isEnabled = enabled
            )
        )

    suspend fun clearLimit(packageName: String) = focusDao.clearLimit(packageName)

    suspend fun saveSchedule(schedule: FocusScheduleEntity, packages: List<String>) =
        focusDao.saveSchedule(schedule, packages)

    suspend fun deleteSchedule(id: String) = focusDao.deleteSchedule(id)

    suspend fun getSchedulePackages(scheduleId: String): List<String> =
        focusDao.getSchedulePackages().filter { it.scheduleId == scheduleId }
            .map { it.packageName }

    /** Why [packageName] may not open right now, evaluated against live usage. */
    suspend fun blockReason(packageName: String): BlockReason {
        val packages = focusDao.getSchedulePackages()
            .groupBy { it.scheduleId }
            .mapValues { (_, v) -> v.map { it.packageName }.toSet() }
        return enforcer.reasonForBlocking(
            packageName = packageName,
            limits = focusDao.getLimits(),
            schedules = focusDao.getSchedules(),
            schedulePackages = packages
        )
    }

    /**
     * True when any schedule is currently blocking anything, so the launcher can
     * show that focus is active without re-deriving each window.
     */
    fun observeActiveScheduleLabel(): Flow<String?> =
        combine(schedules, schedulePackages) { schedules, packages ->
            val calendar = java.util.Calendar.getInstance()
            val nowMinutes =
                calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            schedules.firstOrNull { schedule ->
                schedule.isEnabled &&
                    packages[schedule.id].orEmpty().isNotEmpty() &&
                    enforcer.isActive(schedule, nowMinutes, dayOfWeek)
            }?.label
        }
}
