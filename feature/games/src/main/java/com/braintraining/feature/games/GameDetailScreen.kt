package com.braintraining.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        if (game != null && skill != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.tertiary)
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .size(40.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(skill!!.name, style = MaterialTheme.typography.bodySmall)
                    Text(game!!.name, style = MaterialTheme.typography.titleLarge)
                    Text(game!!.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {},
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 60.dp, vertical = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Play", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Text("Loading...")
        }
    }

}