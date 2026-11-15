package com.raptrx.still.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.raptrx.still.ui.backup.BackupScreen
import com.raptrx.still.ui.favourite_apps.FavouriteAppsScreen
import com.raptrx.still.ui.focus.AddLimitScreen
import com.raptrx.still.ui.focus.FocusScreen
import com.raptrx.still.ui.favourite_apps.reorder_apps.ReorderAppsScreen
import com.raptrx.still.ui.hidden_apps.HiddenAppsScreen
import com.raptrx.still.ui.home.AppDrawerScreen
import com.raptrx.still.ui.home.HomeScreen
import com.raptrx.still.ui.home.HomeViewModel
import com.raptrx.still.ui.intro.IntroScreen
import com.raptrx.still.ui.launch.LaunchScreen
import com.raptrx.still.ui.settings.SettingsScreen
import com.raptrx.still.ui.settings.SupporterScreen
import com.raptrx.still.ui.settings.about.AboutAppScreen
import com.raptrx.still.ui.settings.customisation.CustomisationScreen

private const val DRAWER_TRANSITION_DURATION_MILLIS = 300
private const val OUTGOING_SCREEN_FADE_DURATION_MILLIS = 50
private const val INCOMING_SCREEN_FADE_DELAY_MILLIS = 50

object Routes {
    const val LAUNCH = "LAUNCH"
    const val INTRO = "INTRO"
    const val HOME = "HOME"
    const val APP_DRAWER = "APP_DRAWER"
    const val SETTINGS = "SETTINGS"
    const val SETTINGS_CUSTOMISATION = "SETTINGS_CUSTOMISATION"
    const val HIDDEN_APPS = "HIDDEN_APPS"
    const val FAVOURITE_APPS = "FAVOURITE_APPS"
    const val FOCUS = "FOCUS"
    const val FOCUS_ADD_LIMIT = "FOCUS_ADD_LIMIT"
    const val BACKUP = "BACKUP"
    const val SETTINGS_REORDER_APPS = "SETTINGS_REORDER_APPS"
    const val SUPPORTER = "SUPPORTER"
    const val ABOUT_APP = "ABOUT_APP"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    enableWallpaper: Boolean,
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
    onBackPressed: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LAUNCH,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { launcherPopEnterTransition() },
        popExitTransition = { launcherPopExitTransition() },
        // Navigation 2.10 defaults predictive Back to scaleOut; match toolbar Back instead.
        predictivePopEnterTransition = { launcherPopEnterTransition() },
        predictivePopExitTransition = { launcherPopExitTransition() }
    ) {
        composable(route = Routes.LAUNCH) {
            LaunchScreen(
                viewModel = hiltViewModel(it),
                onNavigateToRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(route = Routes.INTRO) {
            IntroScreen(
                viewModel = hiltViewModel(it),
                onIntroCompleted = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(
            route = Routes.HOME,
            exitTransition = {
                if (targetState.destination.route == Routes.APP_DRAWER) {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = OUTGOING_SCREEN_FADE_DURATION_MILLIS
                        )
                    ) +
                            slideOutVertically(
                                animationSpec = tween(
                                    durationMillis = DRAWER_TRANSITION_DURATION_MILLIS
                                ),
                                targetOffsetY = { -it / 8 }
                            )
                } else {
                    ExitTransition.None
                }
            }
        ) {
            HomeScreen(
                viewModel = homeViewModel,
                enableWallpaper = enableWallpaper,
                statusBarVisible = statusBarVisible,
                navigationBarVisible = navigationBarVisible,
                onOpenAppDrawer = {
                    navController.navigate(Routes.APP_DRAWER) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onSupporterClick = {
                    navController.navigate(Routes.SUPPORTER) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Routes.APP_DRAWER,
            enterTransition = {
                fadeIn(
                    animationSpec = keyframes {
                        durationMillis = DRAWER_TRANSITION_DURATION_MILLIS
                        // Keep Drawer invisible until Home is fully faded out.
                        0f at INCOMING_SCREEN_FADE_DELAY_MILLIS using FastOutSlowInEasing
                    }
                ) +
                        slideInVertically(
                            animationSpec = tween(
                                durationMillis = DRAWER_TRANSITION_DURATION_MILLIS
                            ),
                            initialOffsetY = { it / 8 }
                        )
            },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { launcherPopExitTransition() }
        ) {
            AppDrawerScreen(
                viewModel = homeViewModel,
                statusBarVisible = statusBarVisible,
                navigationBarVisible = navigationBarVisible,
                onCloseAppDrawer = {
                    if (navController.currentDestination?.route == Routes.APP_DRAWER) {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        composable(route = Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = onBackPressed,
                onFocusClick = {
                    navController.navigate(Routes.FOCUS)
                },
                onHiddenAppsClick = {
                    navController.navigate(Routes.HIDDEN_APPS)
                },
                onCustomisationClick = {
                    navController.navigate(Routes.SETTINGS_CUSTOMISATION)
                },
                onBackupClick = {
                    navController.navigate(Routes.BACKUP)
                },
                onFavouriteAppsClick = {
                    navController.navigate(Routes.FAVOURITE_APPS)
                },
                onSupporterClick = {
                    navController.navigate(Routes.SUPPORTER)
                },
                onAboutAppClick = {
                    navController.navigate(Routes.ABOUT_APP)
                }
            )
        }
        composable(route = Routes.ABOUT_APP) {
            AboutAppScreen(
                onBackClick = onBackPressed
            )
        }
        composable(route = Routes.SUPPORTER) {
            SupporterScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed
            )
        }
        composable(route = Routes.HIDDEN_APPS) {
            HiddenAppsScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed
            )
        }
        composable(route = Routes.SETTINGS_CUSTOMISATION) {
            CustomisationScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed
            )
        }
        composable(route = Routes.FOCUS) {
            FocusScreen(
                onBackClick = onBackPressed,
                onAddLimitClick = {
                    navController.navigate(Routes.FOCUS_ADD_LIMIT)
                }
            )
        }
        composable(route = Routes.FOCUS_ADD_LIMIT) {
            AddLimitScreen(
                onBackClick = onBackPressed,
                onSaved = {
                    navController.popBackStack(Routes.FOCUS, inclusive = false)
                }
            )
        }
        composable(route = Routes.BACKUP) {
            BackupScreen(onBackClick = onBackPressed)
        }
        composable(route = Routes.FAVOURITE_APPS) {
            FavouriteAppsScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed,
                onReorderClick = {
                    navController.navigate(Routes.SETTINGS_REORDER_APPS)
                }
            )
        }
        composable(route = Routes.SETTINGS_REORDER_APPS) {
            ReorderAppsScreen(
                viewModel = hiltViewModel(it),
                onBackClick = onBackPressed,
            )
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.launcherPopEnterTransition(): EnterTransition {
    if (initialState.destination.route != Routes.APP_DRAWER ||
        targetState.destination.route != Routes.HOME
    ) return EnterTransition.None

    return fadeIn(
        animationSpec = keyframes {
            durationMillis = DRAWER_TRANSITION_DURATION_MILLIS
            // Keep Home invisible until Drawer is fully faded out.
            0f at INCOMING_SCREEN_FADE_DELAY_MILLIS using FastOutSlowInEasing
        }
    ) + slideInVertically(
        animationSpec = tween(durationMillis = DRAWER_TRANSITION_DURATION_MILLIS),
        initialOffsetY = { -it / 8 }
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.launcherPopExitTransition(): ExitTransition {
    if (initialState.destination.route != Routes.APP_DRAWER) return ExitTransition.None

    return fadeOut(
        animationSpec = tween(durationMillis = OUTGOING_SCREEN_FADE_DURATION_MILLIS)
    ) + slideOutVertically(
        animationSpec = tween(durationMillis = DRAWER_TRANSITION_DURATION_MILLIS),
        targetOffsetY = { it / 8 }
    )
}
