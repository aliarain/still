package com.raptrx.still.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import android.content.Context
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.raptrx.still.data.entities.FocusLimitEntity
import com.raptrx.still.data.entities.FocusScheduleEntity
import com.raptrx.still.data.entities.FocusSchedulePackageEntity
import com.raptrx.still.data.entities.FolderEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports and restores everything the user configured: launcher layout, focus
 * rules and every preference.
 *
 * The format is plain JSON with a version field so a file written by an older
 * Still can be recognised rather than silently misread.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: DataStore<Preferences>,
    private val appInfoDao: AppInfoDao,
    private val folderDao: FolderDao,
    private val focusDao: FocusDao
) {
    suspend fun exportTo(uri: android.net.Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject()
            root.put("format", FORMAT_NAME)
            root.put("version", FORMAT_VERSION)
            root.put("exportedAt", System.currentTimeMillis())

            root.put("apps", JSONArray().apply {
                appInfoDao.getAllApps().forEach { app ->
                    put(
                        JSONObject().apply {
                            put("packageName", app.packageName)
                            put("itemType", app.itemType.persistedValue)
                            put("targetId", app.targetId)
                            put("userHandle", app.userHandle)
                            put("appName", app.appName)
                            put("alternateAppName", app.alternateAppName)
                            put("isFavourite", app.isFavourite)
                            put("isHidden", app.isHidden)
                            put("orderIndex", app.orderIndex)
                            put("launchDelaySeconds", app.launchDelaySeconds)
                            app.folderId?.let { put("folderId", it) }
                        }
                    )
                }
            })

            root.put("folders", JSONArray().apply {
                folderDao.getFolders().forEach { folder ->
                    put(
                        JSONObject().apply {
                            put("id", folder.id)
                            put("name", folder.name)
                            put("normalizedName", folder.normalizedName)
                            put("isFavourite", folder.isFavourite)
                        }
                    )
                }
            })

            root.put("limits", JSONArray().apply {
                focusDao.getLimits().forEach { limit ->
                    put(
                        JSONObject().apply {
                            put("packageName", limit.packageName)
                            put("dailyLimitMinutes", limit.dailyLimitMinutes)
                            put("isEnabled", limit.isEnabled)
                        }
                    )
                }
            })

            root.put("schedules", JSONArray().apply {
                val schedulePackages = focusDao.getSchedulePackages()
                focusDao.getSchedules().forEach { schedule ->
                    put(
                        JSONObject().apply {
                            put("id", schedule.id)
                            put("label", schedule.label)
                            put("startMinutes", schedule.startMinutes)
                            put("endMinutes", schedule.endMinutes)
                            put("daysMask", schedule.daysMask)
                            put("isEnabled", schedule.isEnabled)
                            put(
                                "packages",
                                JSONArray(
                                    schedulePackages
                                        .filter { it.scheduleId == schedule.id }
                                        .map { it.packageName }
                                )
                            )
                        }
                    )
                }
            })

            val currentPreferences = preferences.data.first()
            root.put("preferences", JSONObject().apply {
                currentPreferences.asMap().forEach { (key, value) ->
                    when (value) {
                        is Boolean -> put(key.name, value)
                        is Int -> put(key.name, value)
                        is Long -> put(key.name, value)
                        is Float -> put(key.name, value.toDouble())
                        is String -> put(key.name, value)
                        is Set<*> -> put(key.name, JSONArray(value.toList()))
                    }
                }
            })

            val payload = root.toString(2)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(payload.toByteArray(Charsets.UTF_8))
            } ?: error("Could not open the selected file for writing")

            payload.length
        }
    }

    suspend fun importFrom(uri: android.net.Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            } ?: error("Could not read the selected file")

            val root = JSONObject(payload)
            require(root.optString("format") == FORMAT_NAME) {
                "That file was not created by Still"
            }
            require(root.optInt("version", 0) <= FORMAT_VERSION) {
                "That backup was created by a newer version of Still"
            }

            var restored = 0

            root.optJSONArray("apps")?.let { apps ->
                val entities = (0 until apps.length()).map { index ->
                    val app = apps.getJSONObject(index)
                    app.toAppInfoEntity()
                }
                if (entities.isNotEmpty()) {
                    appInfoDao.addApps(entities)
                    restored += entities.size
                }
            }

            root.optJSONArray("folders")?.let { folders ->
                val entities = (0 until folders.length()).map { index ->
                    val folder = folders.getJSONObject(index)
                    FolderEntity(
                        id = folder.getString("id"),
                        name = folder.getString("name"),
                        normalizedName = folder.getString("normalizedName"),
                        isFavourite = folder.optBoolean("isFavourite", false)
                    )
                }
                if (entities.isNotEmpty()) {
                    entities.forEach { folderDao.insert(it) }
                    restored += entities.size
                }
            }

            root.optJSONArray("limits")?.let { array ->
                (0 until array.length()).forEach { index ->
                    val limit = array.getJSONObject(index)
                    focusDao.setLimit(
                        FocusLimitEntity(
                            packageName = limit.getString("packageName"),
                            dailyLimitMinutes = limit.getInt("dailyLimitMinutes"),
                            isEnabled = limit.optBoolean("isEnabled", true)
                        )
                    )
                    restored++
                }
            }

            root.optJSONArray("schedules")?.let { array ->
                (0 until array.length()).forEach { index ->
                    val item = array.getJSONObject(index)
                    val schedule = FocusScheduleEntity(
                        id = item.getString("id"),
                        label = item.getString("label"),
                        startMinutes = item.getInt("startMinutes"),
                        endMinutes = item.getInt("endMinutes"),
                        daysMask = item.getInt("daysMask"),
                        isEnabled = item.optBoolean("isEnabled", true)
                    )
                    val packages = item.optJSONArray("packages")?.let { list ->
                        (0 until list.length()).map { list.getString(it) }
                    }.orEmpty()
                    focusDao.saveSchedule(schedule, packages)
                    restored++
                }
            }

            root.optJSONObject("preferences")?.let { prefs ->
                preferences.edit { target ->
                    prefs.keys().forEach { name ->
                        when (val value = prefs.get(name)) {
                            is Boolean -> target[booleanPreferencesKey(name)] = value
                            is Int -> target[intPreferencesKey(name)] = value
                            is Long -> target[longPreferencesKey(name)] = value
                            is String -> target[stringPreferencesKey(name)] = value
                            is JSONArray -> target[stringSetPreferencesKey(name)] =
                                (0 until value.length()).map { value.getString(it) }.toSet()
                            is Double -> target[intPreferencesKey(name)] = value.toInt()
                        }
                    }
                }
            }

            restored
        }
    }

    private fun JSONObject.toAppInfoEntity() =
        com.raptrx.still.data.entities.AppInfoEntity(
            packageName = getString("packageName"),
            itemType = com.raptrx.still.data.entities.AppItemType.fromPersistedValue(
                getString("itemType")
            ),
            targetId = getString("targetId"),
            userHandle = getInt("userHandle"),
            appName = getString("appName"),
            alternateAppName = optString("alternateAppName", ""),
            isFavourite = optBoolean("isFavourite", false),
            isHidden = optBoolean("isHidden", false),
            orderIndex = optInt("orderIndex", 0),
            launchDelaySeconds = optInt("launchDelaySeconds", 0),
            folderId = if (has("folderId")) getString("folderId") else null
        )

    private companion object {
        const val FORMAT_NAME = "still-backup"
        const val FORMAT_VERSION = 1
    }
}
