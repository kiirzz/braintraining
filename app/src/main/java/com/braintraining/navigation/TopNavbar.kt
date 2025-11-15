package com.braintraining.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavbar(
    navController: NavHostController,
//    showBackButton: Boolean = false,
    showTopBar: Boolean = true
) {
    if (showTopBar) {
        TopAppBar(
            title = { },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground
            ),
//            navigationIcon = {
//                if (showBackButton) {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            imageVector = Icons.Filled.ArrowBackIosNew,
//                            contentDescription = "Back"
//                        )
//                    }
//                } else null
//            },
            actions = {
                IconButton(
                    onClick = {
                        navController.navigate(Dest.Account) {
                            launchSingleTop = true
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBox,
                        contentDescription = "Account"
                    )
                }
            }
        )
    }
}
