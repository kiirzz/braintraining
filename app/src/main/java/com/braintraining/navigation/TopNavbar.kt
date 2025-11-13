package com.braintraining.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavbar(
    navController: NavHostController,
    showBackButton: Boolean = false,
    showTopBar: Boolean = true
) {
    if (showTopBar) {
        TopAppBar(
            title = {  },
            navigationIcon = {
                if (showBackButton) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                } else null
            },
            actions = {
                IconButton(onClick = {
                    navController.navigate(Dest.Setting) {
                        launchSingleTop = true
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Setting"
                    )
                }
            }
        )
    }
}
