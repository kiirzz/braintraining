package com.braintraining.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun BottomNavbar(
    navController: NavHostController,
    showBottomBar: Boolean = true
) {
    val items = listOf(
        BottomItem(Dest.Home, "Home", Icons.Filled.Home),
        BottomItem(Dest.Games, "Games", Icons.Filled.VideogameAsset),
        BottomItem(Dest.Stats, "Stats", Icons.Filled.Equalizer)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (showBottomBar) {
        NavigationBar {
            items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(text = item.label) }
                )
            }
        }
    }
}
