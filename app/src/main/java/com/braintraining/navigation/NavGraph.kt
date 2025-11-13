package com.braintraining.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.braintraining.feature.games.GameDetailScreen
import com.braintraining.feature.home.HomeScreen
import com.braintraining.feature.games.GameScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    setShowBackButton: (Boolean) -> Unit,
    setShowTopBar: (Boolean) -> Unit,
    setShowBottomBar: (Boolean) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Dest.Home,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Dest.Home) {
            setShowBackButton(false)
            HomeScreen()
        }
        composable(Dest.Games) {
            setShowBackButton(false)
            GameScreen(navController)
        }
        composable(Dest.Stats) {
            setShowBackButton(false)
            Text("Stats")
        }
        composable(Dest.Setting) {
            setShowBackButton(true)
            Text("Setting")
        }
        composable(Dest.GameDetailWithId) {
            val gameId = it.arguments?.getString("gameId")
            setShowTopBar(false)
            setShowBottomBar(false)
            GameDetailScreen(
                navController = navController,
                gameId = gameId
            )
        }
    }
}