package com.raptrx.still.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Reads per-package foreground time from the system usage service.
 *
 * This is deliberately separate from [ScreenTimeHelper], which reports one
 * total for the whole device. The Focus pack needs a figure per package so a
 * limit can be enforced and so search can rank by real usage.
 */
@Singleton
class AppUsageTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager: UsageStatsManager?
        get() = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    /** Start of the current calendar day in local time. */
    fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /**
     * Foreground milliseconds per package since [start].
     *
     * A package that is still in the foreground at the end of the queried
     * window is credited only up to the last observed event rather than to
     * "now", so a limit does not silently consume unmeasured time.
     */
    fun getForegroundMillisSince(start: Long): Map<String, Long> = try {
        val manager = usageStatsManager ?: return emptyMap()
        val end = System.currentTimeMillis()
        val events = manager.queryEvents(start, end) ?: return emptyMap()

        val resumedAt = mutableMapOf<String, Long>()
        val totals = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            if (!events.getNextEvent(event)) continue
            val packageName = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> resumedAt[packageName] = event.timeStamp
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    resumedAt.remove(packageName)?.let { resumeTime ->
                        val elapsed = max(0L, event.timeStamp - resumeTime)
                        totals[packageName] = (totals[packageName] ?: 0L) + elapsed
                    }
                }
            }
        }

        // The launcher itself is never a usage target, and an app still open when
        // this query ran has no stop event yet, so it contributes nothing further.
        totals.remove(context.packageName)
        totals
    } catch (exception: Exception) {
        Timber.e(exception, "Could not read per-app usage")
        emptyMap()
    }

    /** Foreground milliseconds for one package today. */
    fun getTodayMillis(packageName: String): Long =
        getForegroundMillisSince(startOfToday())[packageName] ?: 0L

    /**
     * Launch counts per package over [windowDays], used to rank search results.
     *
     * Ranking by how often an app is actually opened is more useful than
     * alphabetisation for a launcher, and it needs no stored history.
     */
    fun getLaunchCountsSince(windowDays: Int = 30): Map<String, Int> = try {
        val manager = usageStatsManager ?: return emptyMap()
        val start = System.currentTimeMillis() - windowDays * DAY_MILLIS
        val events = manager.queryEvents(start, System.currentTimeMillis()) ?: return emptyMap()

        val counts = mutableMapOf<String, Int>()
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            if (!events.getNextEvent(event)) continue
            val packageName = event.packageName ?: continue
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                packageName != context.packageName
            ) {
                counts[packageName] = (counts[packageName] ?: 0) + 1
            }
        }
        counts
    } catch (exception: Exception) {
        Timber.e(exception, "Could not read launch counts")
        emptyMap()
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
