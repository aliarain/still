package com.raptrx.still.ui.hidden_apps

import com.raptrx.still.ui.entities.AppInfo
import com.raptrx.still.utils.Constants
import com.raptrx.still.utils.SearchMode

data class HiddenAppsState(
    val allApps: List<AppInfo> = emptyList(),
    val hiddenApps: List<AppInfo> = emptyList(),
    val filteredAllApps: List<AppInfo> = emptyList(),
    val searchText: String = "",
    val searchMode: SearchMode = SearchMode.Contains,
    val searchBarBackground: Boolean = false,
    val searchPreferencesLoaded: Boolean = false,
    val searchBarBorderPercent: Int = Constants.DEFAULT_SEARCH_BAR_BORDER_PERCENT,
    val showAppBarMorePopup: Boolean = false,
    val showHiddenOnly: Boolean = false
)
