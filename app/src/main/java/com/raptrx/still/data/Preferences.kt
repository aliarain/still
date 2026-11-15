package com.raptrx.still.data

import com.raptrx.still.ui.theme.ThemeMode
import com.raptrx.still.utils.AppIconAlignment
import com.raptrx.still.utils.Constants
import com.raptrx.still.utils.FastScrollerAlignment
import com.raptrx.still.utils.HomeAppsAlignmentHorizontal
import com.raptrx.still.utils.HomeAppsAlignmentVertical
import com.raptrx.still.utils.HomeClockAlignment
import com.raptrx.still.utils.HomeClockMode
import com.raptrx.still.utils.HomeDateFormat
import com.raptrx.still.utils.StillSettingsPosition
import com.raptrx.still.utils.ScreenOrientation
import com.raptrx.still.utils.SearchMode

data class SearchPreferences(
    val searchMode: SearchMode = SearchMode.Contains,
    val searchBarBackground: Boolean = false,
    val searchBarBorderPercent: Int = Constants.DEFAULT_SEARCH_BAR_BORDER_PERCENT
)

data class MainPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val fontPreference: String = "",
    val screenOrientation: ScreenOrientation = ScreenOrientation.Portrait,
    val showStatusBar: Boolean = true,
    val showNavigationBar: Boolean = true,
    val dynamicTheme: Boolean = false,
    val blackTheme: Boolean = false,
    val setWallpaperToThemeColor: Boolean = false,
    val enableWallpaper: Boolean = false,
    val enableWallpaperOnDrawer: Boolean = false,
    val dimWallpaper: Boolean = false,
    val dimWallpaperPercentage: Int = Constants.DEFAULT_DIM_WALLPAPER_PERCENTAGE,
    val lightTextOnWallpaper: Boolean = true
)

data class HomePreferences(
    val homeAppsAlignmentHorizontal: HomeAppsAlignmentHorizontal = HomeAppsAlignmentHorizontal.Start,
    val drawerAppsAlignmentHorizontal: HomeAppsAlignmentHorizontal = HomeAppsAlignmentHorizontal.Start,
    val homeAppsAlignmentVertical: HomeAppsAlignmentVertical = HomeAppsAlignmentVertical.Center,
    val homeClockAlignment: HomeClockAlignment = HomeClockAlignment.Start,
    val showHomeClock: Boolean = false,
    val homeTimeTextSize: Int? = null,
    val homeDateTextSize: Int? = null,
    val homeTimeFont: String? = null,
    val homeDateFormat: HomeDateFormat = HomeDateFormat.Default,
    val homeTextSize: Int = Constants.DEFAULT_HOME_TEXT_SIZE,
    val autoOpenKeyboardAllApps: Boolean = false,
    val homeClockMode: HomeClockMode = HomeClockMode.Full,
    val doubleTapToLock: Boolean = false,
    val twentyFourHourFormat: Boolean = false,
    val showBatteryLevel: Boolean = false,
    val showHiddenAppsInSearch: Boolean = true,
    val drawerSearchBarAtBottom: Boolean = false,
    val showAppIconInHome: Boolean = false,
    val showAppIconInDrawer: Boolean = false,
    val homeAppIconAlignment: AppIconAlignment = AppIconAlignment.Left,
    val drawerAppIconAlignment: AppIconAlignment = AppIconAlignment.Left,
    val appIconSizePercent: Int = Constants.DEFAULT_APP_ICON_SIZE_PERCENT,
    val iconPack: String = "",
    val applyHomeAppSizeToAllApps: Boolean = false,
    val autoOpenApp: Boolean = false,
    val homeAppVerticalPadding: Int = Constants.DEFAULT_HOME_VERTICAL_PADDING,
    val ignoreSpecialCharacters: String = "",
    val searchMode: SearchMode = SearchMode.Contains,
    val searchBarBorderPercent: Int = Constants.DEFAULT_SEARCH_BAR_BORDER_PERCENT,
    val searchBarBackground: Boolean = false,
    val hideAppDrawerSearch: Boolean = false,
    val hideSettingsIcon: Boolean = false,
    val minimoSettingsPosition: StillSettingsPosition = StillSettingsPosition.Auto,
    val enableWallpaper: Boolean = false,
    val enableWallpaperOnDrawer: Boolean = false,
    val showScreenTimeWidget: Boolean = false,
    val lightTextOnWallpaper: Boolean = true,
    val clockAppPreference: String = "",
    val batteryAppPreference: String = "",
    val calendarAppPreference: String = "",
    val screenTimeAppPreference: String = "",
    val swipeLeftAppPreference: String = "",
    val swipeRightAppPreference: String = "",
    val keyboardOpenDelay: Long = Constants.DEFAULT_KEYBOARD_OPEN_DELAY,
    val enableFastScroller: Boolean = false,
    val fastScrollerAlignment: FastScrollerAlignment = FastScrollerAlignment.Right,
    val backOpensAppDrawer: Boolean = true,
    val compactAppTouchArea: Boolean = false,
    val keyboardDoneOpensFirstApp: Boolean = false
)

data class CustomisationPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val fontPreference: String = "",
    val iconPack: String = "",
    val screenOrientation: ScreenOrientation = ScreenOrientation.Portrait,
    val homeAppsAlignmentHorizontal: HomeAppsAlignmentHorizontal = HomeAppsAlignmentHorizontal.Start,
    val drawerAppsAlignmentHorizontal: HomeAppsAlignmentHorizontal = HomeAppsAlignmentHorizontal.Start,
    val homeAppsAlignmentVertical: HomeAppsAlignmentVertical = HomeAppsAlignmentVertical.Center,
    val homeClockAlignment: HomeClockAlignment = HomeClockAlignment.Start,
    val showHomeClock: Boolean = false,
    val homeTimeTextSize: Int? = null,
    val homeDateTextSize: Int? = null,
    val homeTimeFont: String? = null,
    val homeDateFormat: HomeDateFormat = HomeDateFormat.Default,
    val showStatusBar: Boolean = true,
    val showNavigationBar: Boolean = true,
    val homeTextSize: Int = Constants.DEFAULT_HOME_TEXT_SIZE,
    val autoOpenKeyboardAllApps: Boolean = false,
    val dynamicTheme: Boolean = false,
    val homeClockMode: HomeClockMode = HomeClockMode.Full,
    val doubleTapToLock: Boolean = false,
    val twentyFourHourFormat: Boolean = false,
    val showBatteryLevel: Boolean = false,
    val showHiddenAppsInSearch: Boolean = true,
    val drawerSearchBarAtBottom: Boolean = false,
    val showAppIconInHome: Boolean = false,
    val showAppIconInDrawer: Boolean = false,
    val homeAppIconAlignment: AppIconAlignment = AppIconAlignment.Left,
    val drawerAppIconAlignment: AppIconAlignment = AppIconAlignment.Left,
    val appIconSizePercent: Int = Constants.DEFAULT_APP_ICON_SIZE_PERCENT,
    val applyHomeAppSizeToAllApps: Boolean = false,
    val blackTheme: Boolean = false,
    val setWallpaperToThemeColor: Boolean = false,
    val enableWallpaper: Boolean = false,
    val enableWallpaperOnDrawer: Boolean = false,
    val lightTextOnWallpaper: Boolean = true,
    val dimWallpaper: Boolean = false,
    val dimWallpaperPercentage: Int = Constants.DEFAULT_DIM_WALLPAPER_PERCENTAGE,
    val autoOpenApp: Boolean = false,
    val notificationDot: Boolean = false,
    val homeAppVerticalPadding: Int = Constants.DEFAULT_HOME_VERTICAL_PADDING,
    val ignoreSpecialCharacters: String = "",
    val searchMode: SearchMode = SearchMode.Contains,
    val searchBarBorderPercent: Int = Constants.DEFAULT_SEARCH_BAR_BORDER_PERCENT,
    val searchBarBackground: Boolean = false,
    val hideAppDrawerSearch: Boolean = false,
    val hideSettingsIcon: Boolean = false,
    val minimoSettingsPosition: StillSettingsPosition = StillSettingsPosition.Auto,
    val showScreenTimeWidget: Boolean = false,
    val clockAppPreference: String = "",
    val batteryAppPreference: String = "",
    val calendarAppPreference: String = "",
    val screenTimeAppPreference: String = "",
    val swipeLeftAppPreference: String = "",
    val swipeRightAppPreference: String = "",
    val keyboardOpenDelay: Long = Constants.DEFAULT_KEYBOARD_OPEN_DELAY,
    val enableFastScroller: Boolean = false,
    val fastScrollerAlignment: FastScrollerAlignment = FastScrollerAlignment.Right,
    val backOpensAppDrawer: Boolean = true,
    val compactAppTouchArea: Boolean = false,
    val keyboardDoneOpensFirstApp: Boolean = false
)
