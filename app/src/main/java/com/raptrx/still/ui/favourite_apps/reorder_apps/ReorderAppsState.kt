package com.raptrx.still.ui.favourite_apps.reorder_apps

import com.raptrx.still.ui.entities.AppInfo

data class ReorderAppsState(
    val apps: List<AppInfo> = emptyList()
)