package com.braintraining.feature.games

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun GameDetailScreen(
    navController: NavHostController,
    gameId: String,
    skillId: String,
    gameViewModel: GameViewModel = hiltViewModel()
) {
    val game by gameViewModel.game.collectAsState()
    val skill by gameViewModel.skill.collectAsState()


    LaunchedEffect(gameId, skillId) {
        gameId.let { gameViewModel.getGame(it) }
        skillId.let { gameViewModel.getSkill(it) }
    }

    Column {
        Button(
            onClick = { navController.popBackStack() }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back"
            )
        }

        if (game != null && skill != null) {
            Text(game!!.name)
            Text(game!!.description)
            Text(skill!!.name)
        } else {
            Text("Loading...")
        }
    }

}