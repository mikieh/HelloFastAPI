package com.callfinder.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.callfinder.app.presentation.home.HomeScreen
import com.callfinder.app.presentation.player.PlayerScreen
import com.callfinder.app.presentation.recordings.RecordingsScreen
import com.callfinder.app.presentation.search.SearchScreen
import com.callfinder.app.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRecordings = {
                    navController.navigate(Screen.Recordings.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Recordings.route) {
            RecordingsScreen(
                onNavigateToPlayer = { recordingId ->
                    navController.navigate(Screen.Player.createRoute(recordingId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToPlayer = { recordingId ->
                    navController.navigate(Screen.Player.createRoute(recordingId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("recordingId") { type = NavType.StringType }
            )
        ) {
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
