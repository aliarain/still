package com.raptrx.still.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.raptrx.still.R
import com.raptrx.still.data.FocusRepository
import com.raptrx.still.ui.focus.formatMinutes
import com.raptrx.still.ui.main.MainActivity
import com.raptrx.still.utils.ScreenTimeHelper
import com.raptrx.still.utils.isAppUsagePermissionGranted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home screen widget surfacing the same numbers the Focus screen uses.
 *
 * Updates happen on the provider's own schedule plus whenever the launcher
 * refreshes it, so the widget stays close to the live usage figure without
 * polling in the background.
 */
@AndroidEntryPoint
class FocusWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var screenTimeHelper: ScreenTimeHelper

    @Inject
    lateinit var focusRepository: FocusRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            render(context, appWidgetManager, widgetId)
        }
    }

    private fun render(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_focus)

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppIntent)

        // Usage access is a user-granted special permission, so the widget has to
        // explain itself rather than silently showing zero.
        if (!context.isAppUsagePermissionGranted()) {
            views.setTextViewText(R.id.widget_screen_time, context.getString(R.string.widget_no_data))
            views.setTextViewText(
                R.id.widget_focus_state,
                context.getString(R.string.widget_needs_usage_access)
            )
            views.setViewVisibility(R.id.widget_limit_one, View.GONE)
            views.setViewVisibility(R.id.widget_limit_two, View.GONE)
            appWidgetManager.updateAppWidget(widgetId, views)
            return
        }

        val todayMillis = screenTimeHelper.getTodayScreenTimeMillis()
        views.setTextViewText(
            R.id.widget_screen_time,
            formatMinutes(todayMillis / 60_000L)
        )

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            val statuses = focusRepository.observeLimitStatuses().first()
            val activeLabel = focusRepository.observeActiveScheduleLabel().first()

            views.setTextViewText(
                R.id.widget_focus_state,
                when {
                    activeLabel != null -> "Focus on: $activeLabel"
                    statuses.isEmpty() -> "No limits set"
                    else -> "${statuses.count { it.isExhausted }} of ${statuses.size} limits used up"
                }
            )

            val nearest = statuses.take(2)
            val one = nearest.getOrNull(0)
            val two = nearest.getOrNull(1)

            if (one == null) {
                views.setViewVisibility(R.id.widget_limit_one, View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_limit_one, View.VISIBLE)
                views.setTextViewText(R.id.widget_limit_one, describe(context, one))
            }

            if (two == null) {
                views.setViewVisibility(R.id.widget_limit_two, View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_limit_two, View.VISIBLE)
                views.setTextViewText(R.id.widget_limit_two, describe(context, two))
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun describe(
        context: Context,
        status: com.raptrx.still.data.LimitStatus
    ): String {
        val label = runCatching {
            val info = context.packageManager.getApplicationInfo(status.packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(status.packageName)

        return if (status.isExhausted) {
            "$label — used up"
        } else {
            "$label — ${formatMinutes(status.remainingMinutes)} left"
        }
    }

    companion object {
        /** Called by the launcher so the widget reflects a launch immediately. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, FocusWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    Intent(context, FocusWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                )
            }
        }
    }
}
