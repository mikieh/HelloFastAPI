package com.callfinder.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Recordings : Screen("recordings")
    data object Search : Screen("search")
    data object Player : Screen("player/{recordingId}") {
        fun createRoute(recordingId: String) = "player/$recordingId"
    }
    data object Settings : Screen("settings")
}
