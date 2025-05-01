package com.kapilagro.sasyak.presentation.common.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kapilagro.sasyak.presentation.auth.LoginScreen
import com.kapilagro.sasyak.presentation.auth.SplashScreen
import com.kapilagro.sasyak.presentation.auth.AuthViewModel
import com.kapilagro.sasyak.presentation.home.HomeScreen
import com.kapilagro.sasyak.presentation.home.HomeViewModel
import com.kapilagro.sasyak.presentation.notifications.NotificationScreen
import com.kapilagro.sasyak.presentation.profile.EditProfileScreen
import com.kapilagro.sasyak.presentation.profile.ProfileScreen
import com.kapilagro.sasyak.presentation.reports.ReportScreen
import com.kapilagro.sasyak.presentation.scanner.ScanResultScreen
import com.kapilagro.sasyak.presentation.scanner.ScannerScreen
import com.kapilagro.sasyak.presentation.tasks.CreateTaskScreen
import com.kapilagro.sasyak.presentation.tasks.TaskDetailScreen
import com.kapilagro.sasyak.presentation.tasks.TaskListScreen
import com.kapilagro.sasyak.presentation.weather.WeatherDetailScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {


        // weather screen

        composable(Screen.WeatherDetail.route) {
            // Get the HomeViewModel to access weather data
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
            val weatherState by homeViewModel.weatherState.collectAsState()

            when (weatherState) {
                is HomeViewModel.WeatherState.Success -> {
                    WeatherDetailScreen(
                        weatherInfo = (weatherState as HomeViewModel.WeatherState.Success).weatherInfo,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    // Handle loading or error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Weather data not available")
                    }
                }
            }
        }

        // Auth screens
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Main screens with bottom navigation
        composable(Screen.Home.route) {
            HomeScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onCreateTaskClick = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onScannerClick = {
                    navController.navigate(Screen.Scanner.route)
                },
onNotificationClick = {
    navController.navigate(Screen.Notifications.route)
},
onWeatherDetailsClick = {
    navController.navigate(Screen.WeatherDetail.route)
}



            )
        }

        composable(Screen.Reports.route) {
            ReportScreen()
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfileClick = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Task related screens
        composable(Screen.TaskList.route) {
            TaskListScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                },
                onCreateTaskClick = {
                    navController.navigate(Screen.CreateTask.route)
                }
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            TaskDetailScreen(
                taskId = taskId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateTask.route) {
            CreateTaskScreen(
                onTaskCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Scanner related screens
        composable(Screen.Scanner.route) {
            ScannerScreen(
                onImageCaptured = {
                    navController.navigate(Screen.ScanResult.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ScanResult.route) {
            ScanResultScreen(
                onCreateTaskClick = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onDoneClick = {
                    navController.popBackStack(Screen.Home.route, false)
                }
            )
        }

        // Profile related screens
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onProfileUpdated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }

    // Handle authentication state changes
    LaunchedEffect(authState) {
        if (!authState && navController.currentDestination?.route != Screen.Login.route
            && navController.currentDestination?.route != Screen.Splash.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }
}


