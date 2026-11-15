package com.raptrx.still.data.usecase

import com.raptrx.still.data.AppInfoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoveAppsUseCase @Inject constructor(
    private val appInfoDao: AppInfoDao,
    private val appSyncMutex: AppSyncMutex
) {
    suspend operator fun invoke(packageName: String, userHandle: Int) =
        withContext(Dispatchers.IO) {
            appSyncMutex.withLock {
                val dbApps = appInfoDao.getItemsByPackageName(packageName, userHandle)
                if (dbApps.isNotEmpty()) {
                    appInfoDao.deleteAppsTransaction(dbApps)
                }
        }
    }
}
