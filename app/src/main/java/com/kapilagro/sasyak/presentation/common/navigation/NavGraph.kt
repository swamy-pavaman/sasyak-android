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
import com.kapilagro.sasyak.data.api.ImageUploadService
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.presentation.advice.AdviceScreen
import com.kapilagro.sasyak.presentation.auth.LoginScreen
import com.kapilagro.sasyak.presentation.auth.SplashScreen
import com.kapilagro.sasyak.presentation.auth.AuthViewModel
import com.kapilagro.sasyak.presentation.common.image.ImageCaptureScreen
import com.kapilagro.sasyak.presentation.common.navigation.Screen.FuelRequestScreen
import com.kapilagro.sasyak.presentation.common.navigation.Screen.SprayingRequestScreen
import com.kapilagro.sasyak.presentation.common.navigation.Screen.YieldRequestScreen
import com.kapilagro.sasyak.presentation.fuel.FuelListViewModel
import com.kapilagro.sasyak.presentation.fuel.FuelRequestScreen
import com.kapilagro.sasyak.presentation.fuel.FuelScreen
import com.kapilagro.sasyak.presentation.home.HomeScreen
import com.kapilagro.sasyak.presentation.home.HomeViewModel
import com.kapilagro.sasyak.presentation.notifications.NotificationScreen
import com.kapilagro.sasyak.presentation.profile.EditProfileScreen
import com.kapilagro.sasyak.presentation.profile.ProfileScreen
import com.kapilagro.sasyak.presentation.profile.ProfileViewModel
import com.kapilagro.sasyak.presentation.reports.ReportScreen
import com.kapilagro.sasyak.presentation.scanner.ScanResultScreen
import com.kapilagro.sasyak.presentation.scanner.ScannerScreen
import com.kapilagro.sasyak.presentation.scouting.ScoutingListViewModel
import com.kapilagro.sasyak.presentation.scouting.ScoutingScreen
import com.kapilagro.sasyak.presentation.scouting.ScoutingRequestScreen
import com.kapilagro.sasyak.presentation.scouting.ScoutingTaskDetailScreen
import com.kapilagro.sasyak.presentation.sowing.SowingListViewModel
import com.kapilagro.sasyak.presentation.sowing.SowingRequestScreen
import com.kapilagro.sasyak.presentation.sowing.SowingScreen
import com.kapilagro.sasyak.presentation.sowing.SowingTaskDetailScreen
import com.kapilagro.sasyak.presentation.spraying.SprayingListViewModel
import com.kapilagro.sasyak.presentation.spraying.SprayingRequestScreen
import com.kapilagro.sasyak.presentation.spraying.SprayingScreen
import com.kapilagro.sasyak.presentation.spraying.SprayingTaskDetailScreen
import com.kapilagro.sasyak.presentation.tasks.CreateTaskScreen
import com.kapilagro.sasyak.presentation.tasks.TaskDetailScreen
import com.kapilagro.sasyak.presentation.tasks.TaskListScreen
import com.kapilagro.sasyak.presentation.team.TeamMemberDetailScreen
import com.kapilagro.sasyak.presentation.team.TeamScreen
import com.kapilagro.sasyak.presentation.weather.WeatherDetailScreen
import com.kapilagro.sasyak.presentation.yield.YieldListViewModel
import com.kapilagro.sasyak.presentation.yield.YieldRequestScreen
import com.kapilagro.sasyak.presentation.yield.YieldScreen
import com.kapilagro.sasyak.presentation.yield.YieldTaskDetailScreen
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    imageUploadService: ImageUploadService
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Advice screen
        composable(Screen.Advice.route) {
            AdviceScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Team screens
        composable(Screen.Team.route) {
            TeamScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTeamMemberClick = { teamMemberId ->
                    navController.navigate(Screen.TeamMemberDetail.createRoute(teamMemberId.toString()))
                }
            )
        }

        composable(
            route = Screen.TeamMemberDetail.route,
            arguments = listOf(
                navArgument("teamMemberId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val teamMemberId = backStackEntry.arguments?.getInt("teamMemberId") ?: -1
            TeamMemberDetailScreen(
                teamMemberId = teamMemberId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Scouting Request screen
        composable(Screen.ScoutingRequestScreen.route) {
            val scoutingListViewModel: ScoutingListViewModel = hiltViewModel()
            val homeViewModel: HomeViewModel = hiltViewModel()
            ScoutingRequestScreen(
                onTaskCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController,
                viewModel = scoutingListViewModel,
                homeViewModel = homeViewModel,
                ioDispatcher = ioDispatcher,
                imageUploadService = imageUploadService
            )
        }

        // Fuel screens
        composable(Screen.FuelScreen.route) {
            FuelScreen(
                onTaskCreated = {
                    navController.navigate(FuelRequestScreen.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                }
            )
        }

        composable(FuelRequestScreen.route) {

            val fuelListViewModel: FuelListViewModel = hiltViewModel()
            val homeViewModel: HomeViewModel = hiltViewModel()
            FuelRequestScreen(
                onTaskCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController,
                viewModel = fuelListViewModel,
                homeViewModel = homeViewModel,
                ioDispatcher = ioDispatcher,
                imageUploadService = imageUploadService
            )
        }

        // Sowing screens
        composable(Screen.SowingScreen.route) {
            SowingScreen(
                onTaskCreated = {
                    navController.navigate(Screen.SowingRequestScreen.route)
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SowingRequestScreen.route) {
            val sowingListViewModel: SowingListViewModel = hiltViewModel()
            val homeViewModel: HomeViewModel = hiltViewModel()
            SowingRequestScreen(
                onTaskCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController,
                viewModel = sowingListViewModel,
                homeViewModel = homeViewModel,
                ioDispatcher = ioDispatcher,
                imageUploadService = imageUploadService
            )
        }

        // Image capture screen
        composable(
            route = Screen.ImageCapture.route,
            arguments = listOf(
                navArgument("folder") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folder = backStackEntry.arguments?.getString("folder") ?: "SOWING"
            ImageCaptureScreen(
                folder = folder,
                maxImages = 5,
                onImagesSelected = { images ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedImages", images)
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Spraying screens
        composable(Screen.SprayingScreen.route) {

            SprayingScreen(
                onTaskCreated = {
                    navController.navigate(SprayingRequestScreen.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                }
            )
        }

        composable(SprayingRequestScreen.route) {
            val sprayingListViewModel: SprayingListViewModel = hiltViewModel()
            val homeViewModel: HomeViewModel = hiltViewModel()
            SprayingRequestScreen(
                onTaskCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController,
                viewModel = sprayingListViewModel,
                homeViewModel = homeViewModel,
                ioDispatcher = ioDispatcher,
                imageUploadService = imageUploadService
            )
        }

        // Yield screens
        composable(Screen.YieldScreen.route) {
            YieldScreen(
                onTaskCreated = {
                    navController.navigate(YieldRequestScreen.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                }
            )
        }

        composable(YieldRequestScreen.route) {
            val yieldListViewModel: YieldListViewModel = hiltViewModel()
            val homeViewModel: HomeViewModel = hiltViewModel()
            YieldRequestScreen(
                onTaskCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController,
                viewModel = yieldListViewModel,
                homeViewModel = homeViewModel,
                ioDispatcher = ioDispatcher,
                imageUploadService = imageUploadService
            )
        }

        // Weather screen
        composable(Screen.WeatherDetail.route) {
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
                },
                onScoutingTaskClick = {
                    navController.navigate(Screen.Scouting.route)
                },
                onFuelRequestClick = {
                    navController.navigate(Screen.FuelScreen.route)
                },
                onSowingTaskClick = {
                    navController.navigate(Screen.SowingScreen.route)
                },
                onSprayingTaskClick = {
                    navController.navigate(Screen.SprayingScreen.route)
                },
                onTeamClick = {
                    navController.navigate(Screen.Team.route)
                },
                onYieldTaskClick = {
                    navController.navigate(Screen.YieldScreen.route)
                },
                onReportsClick = {
                    navController.navigate(Screen.Reports.route)
                },
                onAdviceClick = {
                    navController.navigate(Screen.Advice.route)
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
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Scouting.route) {
            ScoutingScreen(
                onTaskCreated = {
                    navController.navigate(Screen.ScoutingRequestScreen.route)
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "sowing_task_detail/{taskId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            SowingTaskDetailScreen(
                taskId = taskId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "yield_task_detail/{taskId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            YieldTaskDetailScreen(
                taskId = taskId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "spraying_task_detail/{taskId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            SprayingTaskDetailScreen(
                taskId = taskId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "scouting_task_detail/{taskId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            ScoutingTaskDetailScreen(
                taskId = taskId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfileClick = {
                    navController.navigate("edit_profile")
                },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.TaskList.route) {
            TaskListScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId.toString()))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController
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

        composable("edit_profile") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            EditProfileScreen(
                onProfileUpdated = {
                    profileViewModel.refreshProfile()
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }

    LaunchedEffect(authState) {
        if (!authState && navController.currentDestination?.route != Screen.Login.route
            && navController.currentDestination?.route != Screen.Splash.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }
}