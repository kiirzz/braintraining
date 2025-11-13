package com.braintraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.braintraining.core.systemdesign.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

import com.braintraining.navigation.BottomNavbar
import com.braintraining.navigation.TopNavbar
import com.braintraining.navigation.NavGraph

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val showBackButtonState = remember { mutableStateOf(false) }
    val showTopBarState = remember { mutableStateOf(true) }
    val showBottomBarState = remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopNavbar(
                navController = navController,
                showBackButton = showBackButtonState.value,
                showTopBar = showTopBarState.value
            )
        },
        bottomBar = {
            BottomNavbar(
                navController = navController,
                showBottomBar = showBottomBarState.value
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavGraph(
                navController = navController,
                paddingValues = innerPadding,
                setShowBackButton = { showBackButtonState.value = it },
                setShowTopBar = { showTopBarState.value = it },
                setShowBottomBar = { showBottomBarState.value = it}
            )
        }
    }
}

