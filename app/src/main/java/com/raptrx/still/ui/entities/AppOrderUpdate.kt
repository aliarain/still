package com.raptrx.still.ui.entities

import com.raptrx.still.data.entities.AppItemType

data class AppOrderUpdate(
    val packageName: String,
    val itemType: AppItemType,
    val targetId: String,
    val userHandle: Int,
    val orderIndex: Int
)
