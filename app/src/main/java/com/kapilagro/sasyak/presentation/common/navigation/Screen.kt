package com.kapilagro.sasyak.presentation.common.navigation

import androidx.annotation.DrawableRes
import com.kapilagro.sasyak.R

sealed class Screen(val route: String, val title: String, @DrawableRes val icon: Int? = null) {
    // Auth screens
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Login")

    // Main screens with bottom navigation
    object Home : Screen("home", "Home", R.drawable.ic_home)
    object Reports : Screen("reports", "Reports", R.drawable.ic_reports)
    object Notifications : Screen("notifications", "Notifications", R.drawable.ic_notifications)
    object Profile : Screen("profile", "Profile", R.drawable.ic_profile)

    // Task related screens
    object TaskList : Screen("task_list", "Tasks")
    object TaskDetail : Screen("task_detail/{taskId}", "Task Detail") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object CreateTask : Screen("create_task", "Create Task")

    // Scanner related screens
    object Scanner : Screen("scanner", "Plant Scanner")
    object ScanResult : Screen("scan_result", "Scan Result")

    // Profile related screens
    object EditProfile : Screen("edit_profile", "Edit Profile")

    companion object {
        val bottomNavItems = listOf(Home, Reports, Notifications, Profile)
    }
}
