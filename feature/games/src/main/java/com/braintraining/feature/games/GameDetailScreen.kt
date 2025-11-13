package com.braintraining.feature.games

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.navigation.NavHostController

@Composable
fun GameDetailScreen(
    navController: NavHostController,
    gameId: String?
) {
    Column {
        Button(
            onClick = { navController.popBackStack() }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back"
            )
        }
        Text(text = "Game Detail: $gameId")
    }

}