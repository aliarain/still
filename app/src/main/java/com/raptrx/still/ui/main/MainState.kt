package com.raptrx.still.ui.main

import com.raptrx.still.ui.theme.ThemeMode
import com.raptrx.still.utils.Constants
import com.raptrx.still.utils.ScreenOrientation

data class MainState(
    val initialLoaded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val fontPreference: String = "",
    val screenOrientation: ScreenOrientation = ScreenOrientation.Portrait,
    val statusBarVisible: Boolean = true,
    val navigationBarVisible: Boolean = true,
    val useDynamicTheme: Boolean = false,
    val blackTheme: Boolean = false,
    val setWallpaperToThemeColor: Boolean = false,
    val enableWallpaper: Boolean = false,
    val enableWallpaperOnDrawer: Boolean = false,
    val dimWallpaper: Boolean = false,
    val dimWallpaperPercentage: Int = Constants.DEFAULT_DIM_WALLPAPER_PERCENTAGE,
    val lightTextOnWallpaper: Boolean = true,
)
