package com.kapilagro.sasyak.presentation.common.components


import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(
    navController: NavController,
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Screen.bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            var badgeCount: Int? = null

            // Show notification badge if there are unread notifications
            if (screen.route == Screen.Notifications.route && unreadNotificationCount > 0) {
                badgeCount = unreadNotificationCount
            }

            NavigationBarItem(
                icon = {
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
                            painter = screen.icon?.let {
                                painterResource(id = it)
                            } ?: return@BadgedBox,
                            contentDescription = screen.title,
                            modifier = Modifier.size(32.dp),  // Increase from 26.dp to 32.dp or your preferred size
                            tint = if (selected) AgroPrimary else Color.Gray
                        )
                    }
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reelecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}