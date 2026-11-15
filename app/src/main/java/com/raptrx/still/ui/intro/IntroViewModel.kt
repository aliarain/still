package com.raptrx.still.ui.intro

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raptrx.still.data.AppInfoDao
import com.raptrx.still.data.PreferenceHelper
import com.raptrx.still.data.usecase.UpdateAllAppsUseCase
import com.raptrx.still.data.usecase.UpdateAllShortcutsUseCase
import com.raptrx.still.ui.entities.AppInfo
import com.raptrx.still.utils.AppUtils
import com.raptrx.still.utils.Constants.INTRO_MINIMUM_FAVOURITE_COUNT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val updateAllAppsUseCase: UpdateAllAppsUseCase,
    private val updateAllShortcutsUseCase: UpdateAllShortcutsUseCase,
    private val appInfoDao: AppInfoDao,
    private val appUtils: AppUtils,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    private val _state = MutableStateFlow(IntroState())
    val state: StateFlow<IntroState> = _state
    val searchState = TextFieldState()

    init {
        viewModelScope.launch {
            snapshotFlow { searchState.text.toString().trim() }.collect(::onSearchTextChange)
        }

        viewModelScope.launch {
            preferenceHelper.getSearchPreferencesFlow()
                .collect { prefs ->
                    _state.update {
                        it.copy(
                            searchPreferencesLoaded = true,
                            searchMode = prefs.searchMode,
                            searchBarBackground = prefs.searchBarBackground,
                            searchBarBorderPercent = prefs.searchBarBorderPercent,
                            filteredAllApps = appUtils.getAppsWithSearch(
                                searchText = it.searchText,
                                apps = it.allApps,
                                searchMode = prefs.searchMode
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            updateAllAppsUseCase.invoke()
        }

        viewModelScope.launch {
            updateAllShortcutsUseCase.invoke()
        }

        viewModelScope.launch {
            appInfoDao.getAllNonHiddenAppsFlow()
                .collect { appInfoList ->
                    val allApps = appUtils.mapToAppInfo(appInfoList)
                    val minimumFavouriteAdded =
                        allApps.count { it.isFavourite } >= INTRO_MINIMUM_FAVOURITE_COUNT

                    _state.update {
                        it.copy(
                            allApps = allApps,
                            filteredAllApps = appUtils.getAppsWithSearch(
                                searchText = it.searchText,
                                apps = allApps,
                                searchMode = it.searchMode
                            ),
                            minimumFavouriteAdded = minimumFavouriteAdded
                        )
                    }
                }
        }
    }

    fun onToggleFavouriteAppClick(appInfo: AppInfo) {
        viewModelScope.launch {
            appInfoDao.setIndividualFavourite(
                appInfo.itemType,
                appInfo.targetId,
                appInfo.packageName,
                appInfo.userHandle,
                isFavourite = !appInfo.isFavourite
            )
        }
    }

    fun setIntroCompleted() {
        viewModelScope.launch {
            preferenceHelper.setIsIntroCompleted(true)
        }
    }

    private fun onSearchTextChange(searchText: String) {
        _state.update {
            it.copy(
                searchText = searchText,
                filteredAllApps = appUtils.getAppsWithSearch(
                    searchText = searchText,
                    apps = it.allApps,
                    searchMode = it.searchMode
                )
            )
        }
    }
}
