package com.raptrx.still.data.usecase

import android.content.pm.ShortcutInfo
import com.raptrx.still.data.AppInfoDao
import com.raptrx.still.data.entities.AppInfoEntity
import com.raptrx.still.data.entities.AppItemType
import com.raptrx.still.utils.InstalledShortcut
import com.raptrx.still.utils.ShortcutInventory
import com.raptrx.still.utils.ShortcutsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateAllShortcutsUseCase @Inject constructor(
    private val shortcutsUtils: ShortcutsUtils,
    private val appInfoDao: AppInfoDao,
    private val appSyncMutex: AppSyncMutex
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        appSyncMutex.withLock {
            val dbShortcuts = appInfoDao.getItemsByType(AppItemType.SHORTCUT)
            val syncResult = syncPinnedShortcuts(
                shortcutsUtils.getInstalledShortcuts(),
                dbShortcuts
            ) ?: return@withLock

            appInfoDao.syncShortcutsTransaction(
                syncResult.updates, syncResult.additions, syncResult.deletions
            )
        }
    }

    suspend fun addAcceptedShortcut(shortcut: ShortcutInfo): Unit = withContext(Dispatchers.IO) {
        appSyncMutex.withLock {
            val installedShortcut = shortcutsUtils.mapInstalledShortcut(shortcut)
            appInfoDao.acceptShortcut(createShortcutEntity(installedShortcut))
        }
    }

}

internal data class ShortcutSyncResult(
    val additions: List<AppInfoEntity>,
    val updates: List<AppInfoEntity>,
    val deletions: List<AppInfoEntity>
)

internal fun syncPinnedShortcuts(
    inventory: ShortcutInventory?,
    dbShortcuts: List<AppInfoEntity>
): ShortcutSyncResult? {
    if (inventory == null) return null

    val installedMap = inventory.shortcuts.associateBy { it.id }
    val updates = mutableListOf<AppInfoEntity>()
    val deletions = mutableListOf<AppInfoEntity>()

    for (dbShortcut in dbShortcuts) {
        val profileRemoved = dbShortcut.userHandle !in inventory.currentProfiles
        val profileQueried = dbShortcut.userHandle in inventory.successfulProfiles
        if (!profileRemoved && !profileQueried) continue

        val installedShortcut = installedMap[dbShortcut.id]
        if (installedShortcut == null) {
            deletions.add(dbShortcut)
        } else {
            updates.add(dbShortcut.copy(appName = installedShortcut.appName))
        }
    }

    val dbIds = dbShortcuts.mapTo(mutableSetOf()) { it.id }
    val additions = inventory.shortcuts
        .filterNot { it.id in dbIds }
        .map(::createShortcutEntity)

    return ShortcutSyncResult(additions, updates, deletions)
}

private fun createShortcutEntity(shortcut: InstalledShortcut): AppInfoEntity {
    return AppInfoEntity(
        packageName = shortcut.packageName,
        itemType = AppItemType.SHORTCUT,
        targetId = shortcut.shortcutId,
        userHandle = shortcut.userHandle,
        appName = shortcut.appName,
        alternateAppName = "",
        isFavourite = false,
        isHidden = false,
        orderIndex = 0,
        launchDelaySeconds = 0
    )
}
