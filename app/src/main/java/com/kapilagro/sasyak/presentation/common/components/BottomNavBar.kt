package com.kapilagro.sasyak.presentation.common.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kapilagro.sasyak.presentation.common.navigation.Screen
import com.kapilagro.sasyak.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(
    navController: NavController,
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                clip = false
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = Card, // Pure white surface
        contentColor = Foreground, // Professional dark gray
        tonalElevation = 4.dp
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
                                Badge(
                                    containerColor = Error, // Clear red for notifications
                                    contentColor = White
                                ) {
                                    Text(
                                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
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
                            modifier = Modifier.size(26.dp), // Slightly smaller for elegance
                            tint = if (selected) AgroPrimary else AgroMutedForeground // Deep forest vs muted green-gray
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) AgroPrimary else AgroMutedForeground
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AgroPrimary, // Deep forest green when selected
                    selectedTextColor = AgroPrimary,
                    unselectedIconColor = AgroMutedForeground, // Muted green-gray when unselected
                    unselectedTextColor = AgroMutedForeground,
                    indicatorColor = AgroLight // Very light green indicator
                )
            )
        }
    }
}