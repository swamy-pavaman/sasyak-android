package com.kapilagro.sasyak

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kapilagro.sasyak.data.api.ImageUploadService
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.presentation.common.navigation.AppNavGraph
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.SasyakTheme
import com.kapilagro.sasyak.presentation.notifications.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var imageUploadService: ImageUploadService

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SasyakTheme {
                MainScreen(ioDispatcher, imageUploadService)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    ioDispatcher: CoroutineDispatcher,
    imageUploadService: ImageUploadService
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val unreadCountState by notificationViewModel.unreadCountState.collectAsState()

    val showBottomBar = remember(currentDestination) {
        currentDestination?.route in Screen.bottomNavItems.map { it.route }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        var badgeCount: Int? = null

                        // Check unread notification count
                        if (screen.route == Screen.Notifications.route &&
                            unreadCountState is NotificationViewModel.UnreadCountState.Success
                        ) {
                            val count = (unreadCountState as NotificationViewModel.UnreadCountState.Success).count
                            if (count > 0) badgeCount = count
                        }

                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { iconId ->
                                    BadgedBox(
                                        badge = {
                                            if (badgeCount != null && badgeCount > 0) {
                                                Badge {
                                                    Text(
                                                        text = if (badgeCount > 99) "99+" else badgeCount.toString()
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = iconId),
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(25.dp)
                                        )
                                    }
                                }
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavGraph(
                navController = navController,
                ioDispatcher = ioDispatcher,
                imageUploadService = imageUploadService
            )
        }
    }

    LaunchedEffect(Unit) {
        notificationViewModel.loadUnreadCount()
    }
}