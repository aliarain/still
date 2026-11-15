package com.raptrx.still.utils

import com.raptrx.still.data.PreferenceHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDotsNotifier @Inject constructor(
    preferenceHelper: PreferenceHelper
) {
    private val _notificationDots = MutableStateFlow<List<NotificationDot>>(emptyList())
    val notificationDots: Flow<List<NotificationDot>> = combine(
        _notificationDots,
        preferenceHelper.getNotificationDot().distinctUntilChanged()
    ) { notificationDots, enable ->
        if (enable) notificationDots else emptyList()
    }.distinctUntilChanged()

    fun updateNotificationDots(notificationDots: List<NotificationDot>) {
        _notificationDots.value = notificationDots
    }
}

data class NotificationDot(
    val packageName: String,
    val userHandle: Int
)
