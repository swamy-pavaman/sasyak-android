package com.kapilagro.sasyak.presentation.common.navigation

import androidx.annotation.DrawableRes
import com.kapilagro.sasyak.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode

sealed class Screen(val route: String, val title: String, @DrawableRes val icon: Int? = null) {
    // Weather
    object WeatherDetail : Screen("weather_detail", "Weather Details")

    // Auth screens
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Login")

    // Activity screens
    object Yield : Screen("yield", "Yield")
    object Fuel : Screen("fuel", "Fuel")
    object Sowing : Screen("sowing", "Sowing")
    object Spraying : Screen("spraying", "Spraying")

    object Sync : Screen("sync", "Sync")

    // Main screens with bottom navigation
    object Home : Screen("home", "Home", R.drawable.ic_home)
    object TaskList : Screen("task_list", "Tasks", R.drawable.ic_baseline_analytics_24)
    object Scanner : Screen("scanner", "Scanner", R.drawable.ic_baseline_image_search)
    object Profile : Screen("profile", "Profile", R.drawable.ic_person)
    object Reports : Screen("reports", "Reports")

    // Notifications
    object Notifications : Screen("notifications", "Notifications", R.drawable.ic_notification)

    // Request screens
    object SowingRequestScreen : Screen("sowing_request", "Sowing Request")
    object SprayingRequestScreen : Screen("spraying_request", "Spraying Request")
    object Scouting : Screen("scouting", "Scouting")
    object SprayingScreen : Screen("spraying", "Spraying")
    object SowingScreen : Screen("sowing", "Sowing")
    object FuelScreen : Screen("fuel", "Fuel")
    object YieldScreen : Screen("yield", "Yield")
    object ScoutingRequestScreen : Screen("scouting_request", "Scouting Request")
    object FuelRequestScreen : Screen("fuel_request_screen", "Fuel Request")
    object YieldRequestScreen : Screen("yield_request", "Yield Request")

    // Image capture screen
    object ImageCapture : Screen("image_capture/{folder}", "Image Capture") {
        fun createRoute(folder: String) = "image_capture/$folder"
    }

    // Team screens
    object Team : Screen("team", "Team Members")
    object TeamMemberDetail : Screen("team_member_detail/{teamMemberId}", "Team Member Detail") {
        fun createRoute(teamMemberId: String) = "team_member_detail/$teamMemberId"
    }

    // Task related screens
    object TaskDetail : Screen("task_detail/{taskId}", "Task Detail") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object CreateTask : Screen("create_task", "Create Task")

    // Scanner related screens
    object ScanResult : Screen("scan_result", "Scan Result")

    // Profile related screens
    object EditProfile : Screen("edit_profile", "Edit Profile")

    // Advice screen
    object Advice : Screen("advice", "Advice")

    companion object {
        val bottomNavItems = listOf(Home, TaskList, Scanner, Profile)
    }
}